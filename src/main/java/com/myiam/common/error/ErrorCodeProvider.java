package com.myiam.common.error;

/**
 * エラーコードプロバイター
 */
public interface ErrorCodeProvider {

    /**
     * エラーコード取得
     *
     * @return {@link ErrorCode}
     */
    ErrorCode getCode();

}
