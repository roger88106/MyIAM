package com.myiam.module.user.domain.user;

import com.myiam.common.error.BusinessException;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

/**
 * ユーザーの識別情報
 * 
 * @param username ユーザー名
 * @param email    メールアドレス
 */
@ValueObject
public record UserIdentity(String username, String email) {

    /** ユーザー名の正規表現 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

    /** E-Mail の正規表現 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * ユーザーの識別情報
     *
     * @param username ユーザー名
     * @param email    メールアドレス
     */
    public UserIdentity {

        // 必須チェック
        if (username == null && email == null) {
            throw BusinessException.of("Username and email can't be blank", UserErrorCode.INVALID_IDENTITY);
        }

        // フォーマットチェック
        if (!validUsernameFormat(username)) {
            throw BusinessException.of("Username structure is invalid: " + username,
                    UserErrorCode.INVALID_USERNAME_FORMAT);
        }
        if (!validEmailFormat(email)) {
            throw BusinessException.of("Email structure is invalid: " + email, UserErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    /**
     * メールアドレスのフォーマット検証。
     *
     * @param email 検証するメールアドレス
     * @return メールアドレスが有効な場合は {@code true}、それ以外の場合は {@code false}
     */
    private static boolean validEmailFormat(String email) {
        if (email == null) return true;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * ユーザー名のフォーマット検証。
     *
     * @param username 検証するユーザー名
     * @return ユーザー名が有効な場合は {@code true}、それ以外の場合は {@code false}
     */
    private static boolean validUsernameFormat(String username) {
        if (username == null) return true;
        return USERNAME_PATTERN.matcher(username).matches();
    }
}
