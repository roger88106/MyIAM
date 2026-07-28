package com.myiam.module.user.domain.user;

import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorCodeProvider;
import com.myiam.common.error.ErrorType;
import com.myiam.common.message.CommonMessage;
import com.myiam.common.message.I18nMessage;

/**
 * ユーザードメインのエラーコード
 */
public enum UserErrorCode implements ErrorCodeProvider {

    /**
     * ユーザー名のフォーマットが正しくない
     */
    INVALID_USERNAME_FORMAT(ErrorType.VALIDATION_ERROR, CommonMessage.INVALID_FORMAT),
    /**
     * メールアドレスのフォーマットが正しくない
     */
    INVALID_EMAIL_FORMAT(ErrorType.VALIDATION_ERROR, CommonMessage.INVALID_FORMAT),
    /**
     * パスワードのフォーマットが正しくない
     */
    INVALID_PASSWORD_FORMAT(ErrorType.VALIDATION_ERROR, CommonMessage.INVALID_FORMAT),
    /**
     * メールアドレスが正しくない
     */
    INVALID_EMAIL(ErrorType.VALIDATION_ERROR, CommonMessage.INVALID_FORMAT),
    /**
     * パスワードが一致していない
     */
    PASSWORD_NOT_MATCHED(ErrorType.BUSINESS_RULE_ERROR, UserMessage.INVALID_PASSWORD),
    /**
     * ユーザーが既に無効化されている<br />
     * UserID: {userId}
     */
    USER_ALREADY_DISABLED(ErrorType.BUSINESS_RULE_ERROR, UserMessage.USER_ALREADY_DISABLED),
    /**
     * ユーザー {username} は既に存在しています。
     */
    USER_ALREADY_EXISTS(ErrorType.BUSINESS_RULE_ERROR, UserMessage.USER_ALREADY_EXISTS),
    /**
     * ユーザーが存在しない
     */
    USER_NOT_FOUND(ErrorType.NOT_FOUND_ERROR, UserMessage.USER_NOT_FOUND),
    /**
     * パスワードがエンコードされていない
     */
    PASSWORD_NOT_ENCODED(ErrorType.SYSTEM_ERROR, CommonMessage.SYSTEM_ERROR),
    /**
     * パスワードハッシュが空白
     */
    PASSWORD_HASH_IS_BLANK(ErrorType.SYSTEM_ERROR, CommonMessage.SYSTEM_ERROR),
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
    UserErrorCode(ErrorType errorType, I18nMessage message) {
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
