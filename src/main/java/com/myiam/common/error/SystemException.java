package com.myiam.common.error;

import com.myiam.common.utility.error.ErrorUtils;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * システム上予期せぬエラー。 ※HTTP 500系
 */
public class SystemException extends BaseException {

    /**
     * システムエラー
     */
    private static final ErrorCode SYSTEM_ERROR = new ErrorCode("SYSTEM_ERROR", List.of(), ErrorType.SYSTEM_ERROR);

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param codeEnum     エラーコード列挙型
     * @param paramValues  エラーメッセージの引数値（該当する場合）
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(String debugMessage, Throwable cause, @NonNull ErrorCode.Enum codeEnum, Object... paramValues) {

        // エラーコード取得
        ErrorCode errorCode = codeEnum.getErrorCode();

        // メッセージパラメータマップを構築する
        Map<String, String> params = ErrorUtils.buildMessageParams(errorCode, paramValues);


        return new SystemException(debugMessage, cause, errorCode, params);
    }

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(String debugMessage, Throwable cause) {
        return new SystemException(debugMessage, cause, SYSTEM_ERROR, Map.of());
    }

    /**
     * {@code SystemException}の新しいインスタンスを作成して返します。
     *
     * @param cause        原因となった例外
     * @return 指定されたパラメータで初期化された新しい{@code SystemException}インスタンス
     */
    public static SystemException of(Throwable cause) {
        return new SystemException(cause.getMessage() ,cause, SYSTEM_ERROR, Map.of());
    }

    /**
     * コンストラクタ。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param errorCode    エラーコード
     * @param params       エラーメッセージの引数
     */
    private SystemException(String debugMessage, Throwable cause, ErrorCode errorCode, Map<String, String> params) {
        super(debugMessage, cause, errorCode.code(), errorCode.errorType(), params);
    }
}
