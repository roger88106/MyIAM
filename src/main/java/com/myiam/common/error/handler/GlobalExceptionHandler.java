package com.myiam.common.error.handler;

import com.myiam.common.error.CommonErrorCode;
import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorType;
import com.myiam.common.error.exception.BaseException;
import com.myiam.common.error.exception.BusinessException;
import com.myiam.common.error.exception.SystemException;
import com.myiam.common.message.CommonMessage;
import com.myiam.common.message.MessageHelper;
import io.micrometer.tracing.Tracer;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Optional;

/**
 * グローバルエラーハンドラー。
 */
@RestControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * メッセージヘルパー
     */
    private final MessageHelper messageHelper;

    /**
     * トレース
     */
    private final Tracer tracer;

    /**
     * ProblemDetail追加パラメータ：エラーコード
     */
    private static final String DETAIL_PROPERTY_ERROR_CODE = "errorCode";
    /**
     * ProblemDetail追加パラメータ：エラーメッセージ
     */
    private static final String DETAIL_PROPERTY_MESSAGE = "message";
    /**
     * ProblemDetail追加パラメータ：トレース ID
     */
    private static final String DETAIL_PROPERTY_TRACE_ID = "traceId";

    // ============================== 例外ハンドラー ==============================

    /**
     * ビジネス例外のハンドラー
     *
     * @param ex {@link BusinessException} ビジネス例外
     * @return Http 4XX + 例外明細
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex, WebRequest request) {
        // ProblemDetail オブジェクト作成
        ProblemDetail detail = buildDefaultProblemDetail(ex, request);

        // カスタマイズ項目設定
        // エラーコード
        detail.setProperty(DETAIL_PROPERTY_ERROR_CODE, resolveErrorCode(ex).code());
        // メッセージ
        detail.setProperty(DETAIL_PROPERTY_MESSAGE, ex.getErrorDetail().message());

        // 戻り値設定
        return ResponseEntity.status(detail.getStatus()).body(detail);
    }

    /**
     * システム例外のハンドラー
     *
     * @param ex {@link SystemException} システム例外
     * @return Http 5XX + 例外明細
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ProblemDetail> handleSystemException(SystemException ex, WebRequest request) {
        // ToDo: DB登録で追跡？ などの検討

        // トレース情報取得
        ThreadInfo threadInfo = getTraceInfo();

        // ProblemDetail オブジェクト作成
        ProblemDetail detail = buildDefaultProblemDetail(ex, request);

        // カスタマイズ項目設定
        // トレース ID
        detail.setProperty(DETAIL_PROPERTY_TRACE_ID, threadInfo.traceId);

        // 戻り値設定
        return ResponseEntity
                .status(detail.getStatus())
                .body(detail);
    }

    /**
     * 全ての例外のハンドラー
     *
     * @param ex {@link Exception} その他例外
     * @return Http 500 + 例外明細
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllException(Exception ex, WebRequest request) {
        // ToDo: DB登録で追跡？ などの検討

        // トレース情報取得
        ThreadInfo threadInfo = getTraceInfo();

        // HTTP ステータス取得
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // インスタンス取得
        URI instance = null;
        if (request instanceof ServletWebRequest servletWebRequest) {
            instance = URI.create(servletWebRequest.getRequest().getRequestURL().toString());
        }

        // ProblemDetail オブジェクト作成
        ProblemDetail detail = ProblemDetail.forStatus(status);
        // タイトル
        detail.setTitle(status.getReasonPhrase());
        // 明細
        detail.setDetail(messageHelper.getDefaultMessage(CommonMessage.SYSTEM_ERROR.code()));
        // インスタンス
        detail.setInstance(instance);

        // カスタマイズ項目設定
        // トレース ID
        detail.setProperty(DETAIL_PROPERTY_TRACE_ID, threadInfo.traceId);

        logger.error("システムエラーが発生しました。", ex);

        // 戻り値設定
        return ResponseEntity
                .status(status)
                .body(detail);
    }

    // ============================== プライベートメソッド ==============================

    /**
     * ベース例外の デフォルト {@link ProblemDetail} オブジェクト作成
     *
     * @param ex      {@link BaseException}
     * @param request {@link WebRequest}
     * @return {@link ProblemDetail}
     */
    private @NonNull ProblemDetail buildDefaultProblemDetail(BaseException ex, WebRequest request) {
        // HTTP ステータス 取得
        HttpStatus status = resolveHttpStatus(ex);

        // インスタンス取得
        URI instance = null;
        if (request instanceof ServletWebRequest servletWebRequest) {
            instance = URI.create(servletWebRequest.getRequest().getRequestURL().toString());
        }

        // ProblemDetail オブジェクト作成
        ProblemDetail detail = ProblemDetail.forStatus(status);
        // タイトル
        detail.setTitle(status.getReasonPhrase());
        // 明細
        detail.setDetail(messageHelper.getDefaultMessage(ex.getErrorDetail()));
        // インスタンス
        detail.setInstance(instance);

        return detail;
    }

    /**
     * Http ステータス解析
     *
     * @param ex {@link BaseException}
     * @return Http ステータス
     */
    private @NotNull HttpStatus resolveHttpStatus(BaseException ex) {
        ErrorType errorType = ex.getErrorDetail().errorCode().type();

        // 例外種類により HTTP ステータス を戻す
        if (ex instanceof BusinessException) {
            // ビジネス系例外の場合、400系のステータスを戻す
            return switch (errorType) {
                case ErrorType.NOT_FOUND_ERROR -> HttpStatus.NOT_FOUND;
                case ErrorType.CONFLICT_ERROR -> HttpStatus.CONFLICT;
                default -> HttpStatus.BAD_REQUEST;
            };

        } else {
            // システム系例外の場合、500系のステータスを戻す
            return switch (errorType) {
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }
    }

    /**
     * エラーコード解析
     *
     * @param ex {@link BaseException} 自定義例外ベース
     * @return エラーコード
     */
    private @NotNull ErrorCode resolveErrorCode(BaseException ex) {
        var errorCode = ex.getErrorDetail().errorCode();
        return switch (errorCode.exposure()) {
            case EXPOSABLE -> errorCode;
            // ToDo 格下げ関連の処理を再検討
            case DOWNGRADE_REQUIRED -> CommonErrorCode.SYSTEM_ERROR.getCode();
        };
    }

    /**
     * トレース情報取得
     *
     * @return トレース情報
     */
    private ThreadInfo getTraceInfo() {
        return Optional.ofNullable(tracer.currentSpan())
                .map(span -> ThreadInfo.builder()
                        .traceId(span.context().traceId())
                        .spanId(span.context().spanId())
                        .build()
                )
                .orElse(null);
    }

    // ============================== プライベートレコード ==============================

    /**
     * トレース情報
     *
     * @param traceId トレースID
     * @param spanId  スパンID
     */
    @Builder
    private record ThreadInfo(@Nullable String traceId, @Nullable String spanId) {
    }
}
