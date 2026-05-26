package com.myiam.microservices.auth.jwk;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * キャッシュ JWK（JSON Web Key） ソース。<br />
 * JWT ライブラリへ鍵情報を提供する。
 */
@Service
@RequiredArgsConstructor
class CachingJWKSource implements JWKSource<SecurityContext> {

    /** JWK 管理サービス */
    private final JWKManagerService jwkManagerService;

    /**
     * JWKの取得。
     *
     * @param jwkSelector 鍵を選択するための条件
     * @param context セキュリティコンテキスト
     * @return 条件に合致する JWK のリスト ※優先度でソート済み
     */
    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        // 現在保持しているキャッシュから条件に合致する鍵を選択して返す
        return jwkSelector.select(jwkManagerService.getJWKSet());
    }

}
