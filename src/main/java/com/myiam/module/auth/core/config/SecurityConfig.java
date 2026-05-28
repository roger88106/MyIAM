package com.myiam.module.auth.core.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * SpringSecurity のフィルターチェーン構成クラス
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
class SecurityConfig {

    /**
     * 認可処理フィルターチェーン
     *
     * @param http HTTP セキュリティ設定
     * @return 認可サーバー用のフィルタチェーン
     */
    @Bean
    @Order(1)
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
                // デフォルトのJwtCustomizerを設定
                .oauth2ResourceServer((rs) -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * デフォルトフィルターチェーン
     *
     * @param http HTTP セキュリティ設定
     * @param authenticationProvider 認証プロバイダー
     * @return デフォルトのフィルタチェーン
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
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

    /**
     * パスワードエンコーダーの設定。
     *
     * @return パスワードエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 暗号化アルゴリズムのアップグレードを防ぐため、NonUpgradingPasswordEncoderを使用
        return new NonUpgradingPasswordEncoder(
                // DelegatingPasswordEncoder を使用し、複数の暗号化アルゴリズムをサポート
                PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    /**
     * 暗号化アルゴリズムのアップグレードをロックするためのPasswordEncoderラッピング
     */
    private record NonUpgradingPasswordEncoder(PasswordEncoder encoder) implements PasswordEncoder {

        /** アップグレードを防ぐため、upgradeEncodingを上書きする */
        @Override
        public boolean upgradeEncoding(@Nullable String encodedPassword) {
            return false;
        }

        /** encodeをそのまま使用 */
        @Override
        public @Nullable String encode(@Nullable CharSequence rawPassword) {
            return encoder.encode(rawPassword);
        }

        /** DelegatingPasswordEncoder.matchesをそのまま使用 */
        @Override
        public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
            return encoder.matches(rawPassword, encodedPassword);
        }
    }
}
