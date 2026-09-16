package com.myiam.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * リソースサーバーのセキュリティ設定
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class ResourceSecurityConfig {
    /**
     * 認証　フィルターチェン
     *
     * @param http HTTP セキュリティ設定
     * @return 認証処理フィルターチェン
     */
    @Bean
    @Order(SecurityOrder.RESOURCE)
    public SecurityFilterChain resourceServerSecurityFilterChain(
            HttpSecurity http
    ) {
        return http
                // 全ての RESTful API を対象として設定
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .anyRequest().authenticated()
                )
                // CSRFを無効化
                .csrf(AbstractHttpConfigurer::disable)
                // セッションをステートレスに設定
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // リソースサーバー設定
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    /**
     * JWT 認証コンバーター
     *
     * @return JWT 認証コンバーター
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // 権限(permission)用の変換クラス設定
        var permissionsConverter = new JwtGrantedAuthoritiesConverter();
        permissionsConverter.setAuthoritiesClaimName("permissions");
        permissionsConverter.setAuthorityPrefix("");

        // Jwt用の変換クラス設定
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(permissionsConverter);
        return converter;
    }
}
