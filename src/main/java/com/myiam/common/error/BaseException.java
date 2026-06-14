package com.myiam.common.error;

import lombok.Getter;

import java.util.Map;

/**
 * 自定義例外ベース
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /**
     * エラーコード
     */
    private final String errorCode;

    /**
     * メッセージの引数
     */
    private final Map<String, String> params;

    /**
     * エラータイプ
     */
    private final ErrorType errorType;

    /**
     * コンストラクタ。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param errorCode    エラーコード
     * @param errorType    エラータイプ
     * @param params       エラーメッセージの引数
     */
    protected BaseException(String debugMessage, Throwable cause, String errorCode, ErrorType errorType, Map<String, String> params) {
        super(debugMessage, cause);

        this.errorCode = errorCode;
        this.params = params;
        this.errorType = errorType;
    }

}
