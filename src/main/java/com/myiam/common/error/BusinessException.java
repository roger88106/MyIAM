package com.myiam.common.error;

import com.myiam.common.utility.error.ErrorUtils;

import java.util.Map;

/**
 * ビジネス例外 ※HTTP 400系
 */
public class BusinessException extends BaseException {

    /**
     * {@code BusinessException}の新しいインスタンスを作成して返します。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param codeEnum     エラーコード列挙型
     * @param paramValues  エラーメッセージの引数値（該当する場合）
     * @return 指定されたパラメータで初期化された新しい{@code BusinessException}インスタンス
     */
    public static BusinessException of(String debugMessage, ErrorCode.Enum codeEnum, Object... paramValues) {
        // エラーコード取得
        ErrorCode errorCode = codeEnum.getErrorCode();

        // メッセージパラメータマップを構築する
        Map<String, String> params = ErrorUtils.buildMessageParams(errorCode, paramValues);

        return new BusinessException(debugMessage, errorCode, params);
    }



    /**
     * ビジネス例外
     *
     * @param debugMessage デバッグ用メッセージ
     * @param errorCode エラーコード
     * @param params メッセージの引数
     */
    private BusinessException(String debugMessage, ErrorCode errorCode, Map<String, String> params) {
        super(debugMessage, null, errorCode.code(), errorCode.errorType(), params);
    }
}
