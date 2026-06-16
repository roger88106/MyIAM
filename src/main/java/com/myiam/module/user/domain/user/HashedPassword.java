package com.myiam.module.user.domain.user;

import com.myiam.common.error.SystemException;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

/**
 * パスワード情報
 *
 * @param value パスワード (ハッシュ後)
 */
@ValueObject
public record HashedPassword(String value) {

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
    public HashedPassword {
        // パスワードハッシュの検証
        if (value == null || value.isBlank()) {
            throw SystemException.of("password can't be blank", null, UserErrorCode.PASSWORD_HASH_IS_BLANK);
        } else if (!STRUCTURE_PATTERN.matcher(value).matches()) {
            throw SystemException.of("password is not encoded", null, UserErrorCode.PASSWORD_NOT_ENCODED);
        }
    }
}
