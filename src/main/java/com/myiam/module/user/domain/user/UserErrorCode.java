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
     * 識別情報が正しくない
     */
    INVALID_IDENTITY(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * パスワードが正しくない
     */
    INVALID_PASSWORD(List.of(), ErrorType.VALIDATION_ERROR),
    /**
     * ユーザーが既に無効化されている<br />
     * ID: [userId]
     */
    USER_ALREADY_DISABLED(List.of("userId"), ErrorType.BUSINESS_RULE_ERROR),
    /**
     * ユーザーが存在しない
     */
    USER_NOT_FOUND(List.of(), ErrorType.NOT_FOUND_ERROR),
    /**
     * ユーザーの復元に失敗した
     */
    USER_RESTORE_ERROR(List.of(), ErrorType.RESTORE_ERROR),
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
