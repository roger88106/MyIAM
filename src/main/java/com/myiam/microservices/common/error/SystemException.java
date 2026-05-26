package com.myiam.microservices.common.error;

/**
 * システム予期せぬ重大なエラー。
 * この例外が発生した場合、システムが不安定な状態にあるか、発生してはならない技術的なエラーが発生したことを意味します。
 * グローバル例外ハンドラーはこの例外をキャッチし、HTTP 500 を返すべきです。
 */
public class SystemException extends RuntimeException {

    /**
     * コンストラクタ。
     *
     * @param message エラーメッセージ
     */
    public SystemException(String message) {
        super(message);
    }

    /**
     * コンストラクタ。
     *
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
