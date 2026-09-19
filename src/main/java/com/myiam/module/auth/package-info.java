/**
 * 認証・認可モジュール。
 */
@org.springframework.modulith.ApplicationModule(
        id = "auth",
        allowedDependencies = {
                "identity::api",
                "identity::event",
                "permission::api",
                "permission::event"
        }
)
package com.myiam.module.auth;
