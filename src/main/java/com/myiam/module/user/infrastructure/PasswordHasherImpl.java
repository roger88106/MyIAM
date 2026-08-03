package com.myiam.module.user.infrastructure;

import com.myiam.module.user.domain.user.port.PasswordHasher;
import com.myiam.module.user.domain.user.vo.HashedPassword;
import com.myiam.module.user.domain.user.vo.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * パスワードハッシュ化処理クラス
 */
@Component
@RequiredArgsConstructor
class PasswordHasherImpl implements PasswordHasher {

    /**
     * パスワードエンコーダ
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * パスワードをハッシュ化する
     *
     * @param rawPassword パスワード
     * @return ハッシュ化されたパスワード
     */
    @Override
    public HashedPassword hash(RawPassword rawPassword) {
        return new HashedPassword(passwordEncoder.encode(rawPassword.value()));
    }

    /**
     * パスワードが一致するかどうかを判定する
     *
     * @param rawPassword パスワード
     * @param hashedPassword ハッシュ化されたパスワード
     * @return 一致する場合{@code true}、一致しない場合{@code false}
     */
    @Override
    public boolean matches(RawPassword rawPassword, HashedPassword hashedPassword) {
        return passwordEncoder.matches(rawPassword.value(), hashedPassword.value());
    }
}
