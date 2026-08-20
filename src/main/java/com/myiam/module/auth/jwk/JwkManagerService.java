package com.myiam.module.auth.jwk;

import com.myiam.common.error.exception.SystemException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * JWK 管理サービス。
 */
@Service
@RequiredArgsConstructor
class JwkManagerService {

    /**
     * JWK リポジトリ
     */
    private final JwkRepository jwkRepository;

    /**
     * 鍵の有効期間
     */
    @Value("${jwt.key.ttl}")
    private final Duration keyTtl;

    /**
     * 鍵の交換スケジュールCRON
     */
    @Value("${jwt.key.rotation.cron}")
    private final String cron;

    /**
     * JWK セットを取得する。
     *
     * @return JWK セット
     */
    JWKSet getJWKSet() {
        return new JWKSet(jwkRepository.selectKeys());
    }

    /**
     * 初期化処理。<br />
     * アプリケーション起動時にデータベースから最新の鍵情報を確認し、必要に応じてローテーションを行う。
     */
    @Transactional
    public void init() {
        // 現在有効な最新の鍵をビューとして取得し、ドメインモデルに変換
        JWK jwk = jwkRepository.selectKeys().stream()
                .findFirst()
                .orElse(null);

        // 鍵が一件も存在しない場合は、新規生成を実行
        if (jwk == null) {
            jwkRotation();
            return;
        }

        // 必要な場合、ローテーションを実行
        if (needsRotation(jwk, cron)) {
            jwkRotation();
        }
    }

    /**
     * 署名鍵のローテーション<br />
     * 新しい鍵ペアを生成し、データベースに保存する。
     */
    @Transactional
    public void jwkRotation() {
        // 新規JWKを生成
        JWK jwk = generateJwk();

        // データベースへ保存 (store)
        jwkRepository.storeKey(jwk);
    }

    /**
     * JWKの生成
     *
     * @return RS256 形式の JWK
     */
    private JWK generateJwk() {
        try {
            String kid = UUID.randomUUID().toString();
            Instant now = Instant.now();

            // RSA 形式の JWK 生成
            return new RSAKeyGenerator(2048)
                    .keyID(kid)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(keyTtl)))
                    .generate();
        } catch (JOSEException e) {
            throw SystemException.of("Key Generation Error", e);
        }
    }

    /**
     * ローテーションが必要かどうかを判定する。
     *
     * @param jwk            JWK オブジェクト
     * @param cronExpression ローテーションのスケジューリング (Cron式)
     * @return ローテーションが必要な場合は true
     */
    private boolean needsRotation(JWK jwk, String cronExpression) {
        var zoneId = ZoneId.systemDefault();
        var cron = CronExpression.parse(cronExpression);

        // JWK の作成時間から、次のローテーション実行時間を算出
        ZonedDateTime nextRunZoned = cron.next(jwk.getIssueTime().toInstant().atZone(zoneId));

        // 次のローテーション実行時間が null または現在時刻より過去の場合、ローテーションが必要
        return nextRunZoned == null || !nextRunZoned.isAfter(ZonedDateTime.now(zoneId));
    }

}
