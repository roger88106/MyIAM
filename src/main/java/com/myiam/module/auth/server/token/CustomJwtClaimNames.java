package com.myiam.module.auth.server.token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JWT クレーム名。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CustomJwtClaimNames {
    public static final String CLIENT_NAME = "client_name";
}
