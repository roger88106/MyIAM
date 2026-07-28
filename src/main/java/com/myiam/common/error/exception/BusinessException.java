package com.myiam.common.error.exception;

import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorDetail;
import lombok.NonNull;

import java.util.List;

/**
 * ビジネス例外 ※HTTP 400系
 */
public class BusinessException extends BaseException {

    /**
     * {@code BusinessException}の新しいインスタンスを作成して返します。
     *
     * @param errorCode エラーコード
     * @return 指定されたパラメータで初期化された新しい{@code BusinessException}インスタンス
     */
    public static BusinessException of(@NonNull ErrorCode errorCode) {
        return new BusinessException(ErrorDetail.of(errorCode));
    }

    /**
     * {@code BusinessException}の新しいインスタンスを作成して返します。
     *
     * @param errorCode     エラーコード
     * @param messageParams メッセージパラメータ
     * @return 指定されたパラメータで初期化された新しい{@code BusinessException}インスタンス
     */
    public static BusinessException of(@NonNull ErrorCode errorCode, List<Object> messageParams) {
        return new BusinessException(ErrorDetail.of(errorCode, messageParams));
    }

    /**
     * ビジネス例外
     *
     * @param errorDetail エラー明細
     */
    private BusinessException(ErrorDetail errorDetail) {
        super(errorDetail.errorCode().code(), null, errorDetail);
    }
}
