package com.myiam.common.error.exception;

import com.myiam.common.error.ErrorDetail;
import lombok.Getter;

/**
 * 自定義例外ベース
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /**
     * エラー詳細情報
     */
    private final ErrorDetail errorDetail;

    /**
     * コンストラクタ。
     *
     * @param debugMessage デバッグ用メッセージ
     * @param cause        原因となった例外
     * @param errorDetail  エラー詳細情報
     */
    protected BaseException(String debugMessage, Throwable cause, ErrorDetail errorDetail) {
        super(debugMessage, cause);

        this.errorDetail = errorDetail;
    }

}
