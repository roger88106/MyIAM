package com.myiam.common.error;

import com.myiam.common.message.CommonMessage;
import com.myiam.common.message.I18nMessage;

/**
 * 共通エラーコード
 */
public enum CommonErrorCode implements ErrorCodeProvider {

    /**
     * 更新処理排他エラー
     */
    CONCURRENT_MODIFICATION(ErrorType.CONFLICT_ERROR, CommonMessage.CONCURRENT_MODIFICATION),
    /**
     * リポジトリ復元エラー
     */
    RESTORE_ERROR(ErrorType.RESTORE_ERROR, CommonMessage.SYSTEM_ERROR),
    /**
     * システムエラー
     */
    SYSTEM_ERROR(ErrorType.SYSTEM_ERROR, CommonMessage.SYSTEM_ERROR);

    /**
     * エラーコード
     */
    private final ErrorCode errorCode;

    /**
     * エラーコード取得
     *
     * @return {@link ErrorCode}
     */
    @Override
    public ErrorCode getCode() {
        return errorCode;
    }

    /**
     * コンストラクタ
     *
     * @param errorType エラータイプ
     * @param message   エラーメッセージ
     */
    CommonErrorCode(ErrorType errorType, I18nMessage message) {
        errorCode = ErrorCode.of(this.name(), errorType, message);
    }
}
