package com.myiam.module.user.domain.user;

import com.myiam.common.error.BusinessException;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

/**
 * パスワード情報
 *
 * @param value パスワード (ハッシュ後)
 */
@ValueObject
public record Password(String value) {

    /**
     * パスワードハッシュの正規表現 <br />
     *
     * 形式: {hashType}hashValue
     */
    private static final Pattern STRUCTURE_PATTERN = Pattern.compile("^\\{[a-zA-Z0-9]+}\\S+$");

    /**
     * パスワード情報
     *
     * @param value パスワード (ハッシュ後)
     */
    public Password {
        // パスワードハッシュの検証
        if (value == null || value.isBlank()) {
            throw BusinessException.of("Password can't be blank", UserErrorCode.INVALID_PASSWORD);
        } else if (!STRUCTURE_PATTERN.matcher(value).matches()) {
            throw BusinessException.of("Password structure is invalid", UserErrorCode.INVALID_PASSWORD);
        }
    }
}
