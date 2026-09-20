package com.myiam.module.auth.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

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

}
