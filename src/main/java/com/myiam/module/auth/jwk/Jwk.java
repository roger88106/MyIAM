package com.myiam.module.auth.jwk;

import com.myiam.common.error.SystemException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.Builder;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JWK ドメインエンティティ。
 *
 * @param id 識別子
 * @param data 状態データ
 */
@Builder
record Jwk(UUID id, KeyData data) {

    /**
     * 新しい RSA 鍵ペアを生成し、ドメインエンティティとして返す。
     *
     * @param ttl 鍵の有効期間
     * @return 新規生成された JWK
     */
    static Jwk generate(Duration ttl) {
        try {
            String kid = UUID.randomUUID().toString();
            Instant now = Instant.now();

            // RSA 鍵ペア生成
            RSAKey rsaKey = new RSAKeyGenerator(2048)
                    .keyID(kid)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .generate();

            // JWK 作成
            return new Jwk(UUID.randomUUID(), new KeyData(kid, rsaKey, true, now.plus(ttl), now));
        } catch (Exception e) {
            throw SystemException.of("Key Generation Error", e);
        }
    }

    /**
     * ローテーションが必要かどうかを判定する。
     *
     * @param cronExpression ローテーションのスケジューリング (Cron式)
     * @return ローテーションが必要な場合は true
     */
    boolean needsRotation(String cronExpression) {

        var zoneId = ZoneId.systemDefault();
        var cron = CronExpression.parse(cronExpression);

        // JWK の作成時間から、次のローテーション実行時間を算出
        ZonedDateTime nextRunZoned = cron.next(data.createdAt().atZone(zoneId));

        // 次のローテーション実行時間が null または現在時刻より過去の場合、ローテーションが必要
        return nextRunZoned == null || !nextRunZoned.isAfter(ZonedDateTime.now(zoneId));
    }


    /**
     * JWK の状態データを表すレコード。
     *
     * @param kid 鍵ID
     * @param key JWK のキー情報
     * @param isEnabled 有効フラグ
     * @param expiresAt 有効期限
     * @param createdAt 作成時間
     */
    @Builder
    record KeyData(String kid, RSAKey key, boolean isEnabled, Instant expiresAt, Instant createdAt) {
    }
}
