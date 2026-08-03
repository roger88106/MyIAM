package com.myiam.module.user.domain.user.port;

import com.myiam.module.user.domain.user.vo.HashedPassword;
import com.myiam.module.user.domain.user.vo.RawPassword;

/**
 * パスワードハッシュ化処理クラス
 */
public interface PasswordHasher {
    /**
     * パスワードをハッシュ化する
     *
     * @param rawPassword パスワード
     * @return ハッシュ化されたパスワード
     */
    HashedPassword hash(RawPassword rawPassword);

    /**
     * パスワードが一致するかどうかを判定する
     *
     * @param rawPassword パスワード
     * @param hashedPassword ハッシュ化されたパスワード
     * @return 一致する場合{@code true}、一致しない場合{@code false}
     */
    boolean matches(RawPassword rawPassword, HashedPassword hashedPassword);
}
