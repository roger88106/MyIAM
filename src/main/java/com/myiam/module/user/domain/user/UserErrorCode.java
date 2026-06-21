package com.myiam.module.user.domain.user;

import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.ErrorType;
import lombok.Getter;

import java.util.List;

/**
 * ユーザードメインのエラーコード
 */
@Getter
public enum UserErrorCode implements ErrorCode.Enum {

    /**
     * ユーザー名のフォーマットが正しくない
     */
    INVALID_USERNAME_FORMAT(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * メールアドレスのフォーマットが正しくない
     */
    INVALID_EMAIL_FORMAT(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * パスワードのフォーマットが正しくない
     */
    INVALID_PASSWORD_FORMAT(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * 識別情報が正しくない
     */
    INVALID_IDENTITY(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * ユーザーが既に無効化されている<br />
     * ID: {userId}
     */
    USER_ALREADY_DISABLED(List.of("userId"), ErrorType.BUSINESS_RULE_ERROR),
    /**
     * パスワードが一致していない
     */
    PASSWORD_NOT_MATCHED(List.of(), ErrorType.BUSINESS_RULE_ERROR),
    /**
     * ユーザー:{user} は既に存在している
     */
    USER_ALREADY_EXISTS(List.of("user"), ErrorType.BUSINESS_RULE_ERROR),
    /**
     * ユーザーが存在しない
     */
    USER_NOT_FOUND(List.of(), ErrorType.NOT_FOUND_ERROR),
    /**
     * パスワードがエンコードされていない
     */
    PASSWORD_NOT_ENCODED(List.of(), ErrorType.SYSTEM_ERROR),
    /**
     * パスワードハッシュが空白
     */
    PASSWORD_HASH_IS_BLANK(List.of(), ErrorType.SYSTEM_ERROR),
    ;

    /**
     * エラーコード
     */
    private final ErrorCode errorCode;

    /**
     * コンストラクタ
     */
    UserErrorCode(List<String> paramKeys, ErrorType errorType) {
        this.errorCode = new ErrorCode(this.name(), paramKeys, errorType);
    }

}
