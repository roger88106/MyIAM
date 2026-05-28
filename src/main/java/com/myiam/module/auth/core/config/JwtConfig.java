package com.myiam.module.auth.core.config;

import com.myiam.module.auth.authentication.AuthenticationDto;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Comparator;

/**
 * JWT 構成クラス。<br />
 * 認可サーバーが発行する JWT トークンの内容をカスタマイズ。
 */
@Configuration
@RequiredArgsConstructor
class JwtConfig {

    /**
     * JWT エンコーダー。
     *
     * @param jwkSource JWK ソース
     * @return JWT エンコーダー
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwkSource);

        // 署名時、複数の有効キーから最新のキーを選択する
        encoder.setJwkSelector(jwks -> jwks.stream()
                .max(Comparator.comparing(JWK::getIssueTime))
                .orElse(null)
        );

        return encoder;
    }

    /**
     * JWT トークンのカスタマイザー。<br />
     * トークンの発行時に、適切な識別子を設定する。
     *
     * @return OAuth2TokenCustomizer JWT エンコーディングコンテキストのカスタマイザー
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {

            // トークンタイプ取得
            var tokenType = context.getTokenType();
            // 認可許可タイプ取得
            var authorizationGrantType = context.getAuthorizationGrantType();

            // トークンの種類がアクセストークンの場合のみ処理を行う
            if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {

                // クライアント認証（バックエンド間通信）の場合
                if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authorizationGrantType)) {
                    // クライアント認証トークン発行
                    this.setClientAccessToken(context);
                } else {
                    // 上記以外の場合、ユーザー認証トークン発行
                    this.setUserAccessToken(context);
                }
            }
        };
    }

    /**
     * ユーザー認証のアクセストークン生成処理
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setUserAccessToken(JwtEncodingContext context) {

        Authentication principal = context.getPrincipal();
        if (principal == null) return;

        // JWT の "sub" (Subject) クレームにprincipalの名前を設定
        context.getClaims().subject(principal.getName());

        // principalがカスタムのユーザの場合
        if (principal.getPrincipal() instanceof AuthenticationDto.UserView user) {
            // カスタムクレーム：ユーザー名追加
            context.getClaims().claim("user_name", user.username());
        }
    }

    /**
     * クライアント認証のアクセストークン生成処理
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setClientAccessToken(JwtEncodingContext context) {
        // sub にクライアント ID を設定
        context.getClaims().subject(context.getRegisteredClient().getClientId());

        // クライアント名称を設定
        context.getClaims().claim("client_name", context.getRegisteredClient().getClientName());
    }

}
