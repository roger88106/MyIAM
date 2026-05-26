package com.myiam.microservices.auth.jwk;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JWK 管理サービス。
 */
@Service
@RequiredArgsConstructor
class JWKManagerService {

    /**
     * JWK リポジトリ
     */
    private final JwkRepository jwkRepository;

    /** 鍵の有効期間 */
    @Value("${jwt.key.ttl}")
    private final Duration KEY_TTL;

    /** 鍵の交換スケジュールCRON */
    @Value("${jwt.key.rotation.cron}")
    private final String CRON;

    /**
     * JWK セットを取得する。
     *
     * @return JWK セット
     */
    JWKSet getJWKSet() {
        // 有効なキーを取得
        List<Jwk> jwks = jwkRepository.selectKeys();

        // JWK セットに変換
        return toJWKSet(jwks);
    }

    /**
     * 初期化処理。<br />
     * アプリケーション起動時にデータベースから最新の鍵情報を確認し、必要に応じてローテーションを行う。
     */
    @Transactional
    @PostConstruct
    void init() {
        // 現在有効な最新の鍵をビューとして取得し、ドメインモデルに変換
        Jwk jwk = jwkRepository.selectKeys()
                .stream().findFirst().orElse(null);

        // 鍵が一件も存在しない場合は、新規生成を実行
        if (jwk == null) {
            jwkRotation();
            return;
        }

        // 必要な場合、ローテーションがを実行
        if (jwk.needsRotation(CRON)) {
            jwkRotation();
        }
    }

    /**
     * 署名鍵の交替<br />
     * 新しい鍵ペアを生成し、データベースに保存する。保存された時点で、キャッシュ自動クリア（@CacheEvict）が作動する。
     */
    @Transactional
    void jwkRotation() {
        // 新なJWKを取得
        Jwk jwk = Jwk.generate(KEY_TTL);

        // データベースへ保存 (store)
        jwkRepository.storeKey(jwk);
    }

    /**
     * JWK セットへの変換。
     *
     * @param jwks JWK リスト
     * @return 構築された JWK セットオブジェクト
     */
    private JWKSet toJWKSet(List<Jwk> jwks) {
        // フレームワークのJWKに変換  ※com.nimbusds.jose.jwk.JWK
        List<JWK> parsedJwks = new ArrayList<>();
        try {
            for (Jwk jwk : jwks) {
                JWK parsedJwk = JWK.parse(jwk.data().jwkJson().toString());
                parsedJwks.add(parsedJwk);
            }
        } catch (Exception e) {
            throw new JwkException("JWK Parse Error", e);
        }

        // 変換後のJWKリストをJWKSetオブジェクトとして返却
        return new JWKSet(parsedJwks);
    }

}
