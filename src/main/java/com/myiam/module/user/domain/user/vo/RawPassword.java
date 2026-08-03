package com.myiam.module.user.domain.user.vo;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.module.user.domain.user.UserErrorCode;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * ユーザーのパスワード (明文)
 *
 * @param value パスワード文字列
 */
@ValueObject
public record RawPassword(String value) {

    /**
     * パスワードの正規表現パターン
     */
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    /**
     * ユーザーのパスワード (明文)
     *
     * @param value パスワード文字列
     */
    public RawPassword {
        if (value == null || value.isBlank()) {
            throw BusinessException.of(UserErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (!value.matches(PASSWORD_REGEX)) {
            throw BusinessException.of(UserErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    /**
     * パスワード (明文)のフォーマット検証
     *
     * @param rawPassword パスワード (明文)
     * @return 有効な場合は {@code true}、それ以外の場合は {@code false}
     */
    public static boolean isValidFormat(String rawPassword) {
        return rawPassword.matches(PASSWORD_REGEX);
    }
}
