package com.myiam.common.error.exception;

import com.myiam.common.error.CommonErrorCode;
import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorDetail;
import lombok.NonNull;

import java.util.List;

/**
 * システム上予期せぬエラー。 ※HTTP 500系
 */
public class SystemException extends BaseException {

    /**
     * システムエラー
     */
    private static final ErrorDetail SYSTEM_ERROR_DETAIL = ErrorDetail.of(CommonErrorCode.SYSTEM_ERROR.getCode());

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param errorCode    エラーコード
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(String debugMessage, Throwable cause, @NonNull ErrorCode errorCode) {
        return new SystemException(debugMessage, cause, ErrorDetail.of(errorCode));
    }

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage  デバッグ用メッセージ
     * @param cause         原因となった例外
     * @param errorCode     エラーコード
     * @param messageParams メッセージパラメータ
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(String debugMessage, Throwable cause, @NonNull ErrorCode errorCode, List<Object> messageParams) {
        return new SystemException(debugMessage, cause, ErrorDetail.of(errorCode, messageParams));
    }

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(String debugMessage, Throwable cause) {
        return new SystemException(debugMessage, cause, SYSTEM_ERROR_DETAIL);
    }

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param cause 原因となった例外
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(Throwable cause) {
        return new SystemException(cause.getMessage(), cause, SYSTEM_ERROR_DETAIL);
    }

    /**
     * コンストラクタ。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param detail       エラー詳細
     */
    private SystemException(String debugMessage, Throwable cause, ErrorDetail detail) {
        super(debugMessage, cause, detail);
    }
}
