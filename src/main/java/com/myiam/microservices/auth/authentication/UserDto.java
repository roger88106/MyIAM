package com.myiam.microservices.auth.authentication;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

/**
 * ユーザー DTO
 */
public class UserDto {

    /**
     * ユーザー情報
     *
     * @param id ユーザー ID
     * @param username ユーザー名
     */
    @Builder
    public record UserView(String id, String username) implements Principal {

        /**
         * Principal 設定用の実装
         *
         * @return ユーザー ID
         */
        @Override
        public String getName() {
            return id;
        }
    }

    /**
     * 認証認可用のユーザー情報<strong> ※敏感情報入り、使用時注意!</strong>
     *
     * @param id ユーザー ID
     * @param username ユーザー名
     * @param password パスワード
     * @param authorities 権限要素（ROLE, SCOPE等）
     * @param enabled アカウント有効フラグ
     * @param accountNonExpired アカウント有効期限フラグ
     * @param credentialsNonExpired 資格有効期限フラグ
     * @param accountNonLocked アカウントロックフラグ
     */
    @Builder
    record AuthUserView(String id, String username, String password, Collection<? extends GrantedAuthority> authorities,
                        boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
                        boolean accountNonLocked) {


        /**
         * toString する際、パスワードは非表示で出力する
         */
        @Override
        @NonNull
        public String toString() {
            return """
                    AuthUserView[\
                    id='%s', \
                    username='%s', \
                    password='********', \
                    authorities=%s, \
                    enabled=%s, \
                    accountNonExpired=%s, \
                    credentialsNonExpired=%s, \
                    accountNonLocked=%s\
                    ]
                    """.formatted(id, username, authorities, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked);
        }
    }
}
