package com.myiam.module.permission.domain.subjectroles;

import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorCodeProvider;
import com.myiam.common.error.ErrorType;
import com.myiam.common.message.I18nMessage;

/**
 * 権限ドメインのエラーコード
 */
public enum PermissionErrorCode implements ErrorCodeProvider {

    /**
     * ロールが存在しない
     */
    ROLE_NOT_FOUND(ErrorType.NOT_FOUND_ERROR, PermissionMessage.ROLE_NOT_FOUND),
    ;

    /**
     * エラーコード
     */
    private final ErrorCode errorCode;

    /**
     * コンストラクタ
     *
     * @param errorType エラータイプ
     * @param message   メッセージ
     */
    PermissionErrorCode(ErrorType errorType, I18nMessage message) {
        this.errorCode = ErrorCode.of(this.name(), errorType, message);
    }

    /**
     * エラーコード取得
     *
     * @return {@link ErrorCode}
     */
    @Override
    public ErrorCode getCode() {
        return errorCode;
    }
}
