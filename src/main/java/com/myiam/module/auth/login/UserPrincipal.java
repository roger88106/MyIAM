package com.myiam.module.auth.login;

import java.security.Principal;
import java.util.UUID;

/**
 * ユーザープリンシパル。
 *
 * @param userId ユーザー ID
 */
public record UserPrincipal(UUID userId) implements Principal {

    /**
     * プリンシパル名を取得する。
     *
     * @return プリンシパル名 (ユーザー ID)
     */
    @Override
    public String getName() {
        return userId.toString();
    }

    /**
     * ユーザープリンシパルを生成する。
     *
     * @param userId ユーザー ID
     * @return ユーザープリンシパル
     */
    public static UserPrincipal of(UUID userId) {
        return new UserPrincipal(userId);
    }
}
