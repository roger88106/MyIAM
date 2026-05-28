package com.myiam.module.auth.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

/**
 * 認可サーバー構成クラス。<br />
 * エンドポイントのパス設定等を行う。
 */
@Configuration
class AuthorizationServerConfig {

    /**
     * 認可サーバーの全般的な設定。
     *
     * @return 認可サーバー設定オブジェクト
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        // デフォルト設定を使用
        return AuthorizationServerSettings.builder().build();
    }
}
