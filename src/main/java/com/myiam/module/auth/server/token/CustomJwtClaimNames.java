package com.myiam.module.auth.server.token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JWT クレーム名。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CustomJwtClaimNames {
    /**
     * クライアント名。
     */
    public static final String CLIENT_NAME = "client_name";

    /**
     * ロール。
     */
    public static final String ROLES = "roles";

    /**
     * 権限。
     */
    public static final String PERMISSIONS = "permissions";
}
