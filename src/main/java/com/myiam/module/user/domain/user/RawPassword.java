package com.myiam.module.user.domain.user;

import com.myiam.common.error.exception.BusinessException;
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
            throw BusinessException.of(UserErrorCode.INVALID_PASSWORD_FORMAT.getCode());
        }
        if (!value.matches(PASSWORD_REGEX)) {
            throw BusinessException.of(UserErrorCode.INVALID_PASSWORD_FORMAT.getCode());
        }
    }
}
