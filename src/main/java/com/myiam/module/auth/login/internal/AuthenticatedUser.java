package com.myiam.module.auth.login.internal;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * 認証済みユーザ
 *
 * @param id ユーザー ID
 * @param password パスワード
 * @param authorities 権限リスト
 */
@Builder
record AuthenticatedUser(UUID id, Password password, Collection<? extends GrantedAuthority> authorities) {

    static AuthenticatedUser of(UUID id, String password, Collection<? extends GrantedAuthority> authorities) {
        return new AuthenticatedUser(id, new Password(password), authorities);
    }

    /**
     * パスワードの値を取得する
     *
     * @return パスワードの値
     */
    String getPasswordValue() {
        return password.value();
    }

    /**
     * パスワード
     *
     * @param value パスワードの値
     */
    record Password(String value) {

        // toString する際、パスワードは非表示で出力する
        @Override
        public @NonNull String toString() {
            return "********";
        }
    }

}
