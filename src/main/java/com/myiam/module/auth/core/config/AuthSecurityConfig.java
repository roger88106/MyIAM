package com.myiam.module.auth.core.config;

import com.myiam.config.security.SecurityOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * SpringSecurity のフィルターチェーン構成クラス
 */
@Configuration
@EnableWebSecurity
class AuthSecurityConfig {

    /**
     * 認可　フィルターチェーン
     *
     * @param http HTTP セキュリティ設定
     * @return 認可サーバー用のフィルタチェーン
     */
    @Bean
    @Order(SecurityOrder.AUTHORIZATION)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http
    ) {

        // OAuth2 認可サーバー
        var authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer()
                // OIDC (OpenID Connect) を有効化する
                .oidc(Customizer.withDefaults());

        http
                // 認可サーバーのエンドポイントを対象として設定
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                // 認可サーバーの構成
                .with(authorizationServerConfigurer, configurer -> {
                })
                // リクエストの認可設定
                .authorizeHttpRequests((authorize) -> authorize
                        // 認可エンドポイントはユーザー認証を必須とする
                        .requestMatchers("/oauth2/authorize").authenticated()
                        // その経由エンドポイント（トークンエンドポイント等）は各フィルタで認可制御を行うためここでは許可
                        .anyRequest().permitAll())
                // 例外ハンドリングの設定
                .exceptionHandling((exceptions) -> exceptions
                        // 未認証時にHTMLリクエスト（ブラウザ等）の場合はログイン画面にリダイレクトする
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                // デフォルトの JwtCustomizerを設定
                .oauth2ResourceServer((rs) -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * 認証　フィルターチェン
     *
     * @param http HTTP セキュリティ設定
     * @param authenticationProvider 認証プロバイダー
     * @return 認証処理フィルターチェン
     */
    @Bean
    @Order(SecurityOrder.AUTHENTICATION)
    public SecurityFilterChain authenticationSecurityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) {

        http
                // 認証プロバイダー適用
                .authenticationProvider(authenticationProvider)
                // リクエストごとの認可ルールの設定
                .authorizeHttpRequests((authorize) -> authorize
                        // ログインページおよび静的リソース（CSS、JS）は認証不要でアクセス可能
                        .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
                        // その他すべてのリクエストは認証を必須とする
                        .anyRequest().authenticated())
                // フォームログインの設定
                .formLogin(form -> form
                        // カスタムログインページのパスを指定
                        .loginPage("/login")
                        // ログインページへのアクセスを全ユーザーに許可
                        .permitAll());

        return http.build();
    }

}
