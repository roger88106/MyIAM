package com.myiam.config.security;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * デフォルトのセキュリティ設定
 */
@Configuration
class DefaultSecurityConfig {
    /**
     * 認証　フィルターチェン
     *
     * @param http HTTP セキュリティ設定
     * @return 認証処理フィルターチェン
     */
    @Bean
    @Order(SecurityOrder.DEFAULT)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http
    ) {
        // ToDo: "/actuator/**"の扱い再検討
        return http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/actuator/**", "/error/**").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated())
                .build();
    }
}
