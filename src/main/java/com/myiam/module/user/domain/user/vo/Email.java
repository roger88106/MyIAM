package com.myiam.module.user.domain.user.vo;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.module.user.domain.user.UserErrorCode;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

/**
 * ユーザーのメールアドレス
 * 
 * @param value    メールアドレス
 */
@ValueObject
public record Email(String value) {

    /** E-Mail の正規表現 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * ユーザーのメールアドレス
     *
     * @param value    メールアドレス
     */
    public Email {

        // 必須チェック
        if (value == null) {
            throw BusinessException.of(UserErrorCode.INVALID_EMAIL);
        }

        // フォーマットチェック
        if (!isValidFormat(value)) {
            throw BusinessException.of(UserErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    /**
     * メールアドレスのフォーマット検証。
     *
     * @param email 検証するメールアドレス
     * @return メールアドレスが有効な場合は {@code true}、それ以外の場合は {@code false}
     */
    public static boolean isValidFormat(String email) {
        if (email == null) return true;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
