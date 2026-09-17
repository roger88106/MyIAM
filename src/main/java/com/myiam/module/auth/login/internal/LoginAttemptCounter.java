package com.myiam.module.auth.login.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * ログイン失敗カウンター。<br />
 * ユーザー単位の連続失敗回数を Redis で管理する。
 */
@Component
@RequiredArgsConstructor
class LoginAttemptCounter {

    /** Redis 文字列テンプレート */
    private final StringRedisTemplate redisTemplate;

    /** ロックまでの失敗回数 */
    @Value("${app.security.lockout.max-attempts}")
    private int maxAttempts;

    /** 失敗回数のカウント窓 */
    @Value("${app.security.lockout.window}")
    private Duration window;

    /**
     * 失敗カウント キー
     * <ul><li>パラメータ：ユーザー ID</li></ul>
     */
    private static final String REDIS_KEY_FAIL = "login:fail:%s";

    /**
     * 失敗を記録する。
     *
     * @param userId ユーザー ID
     * @return 失敗回数が閾値に達した場合 {@code true}
     */
    boolean recordFailure(UUID userId) {
        String key = REDIS_KEY_FAIL.formatted(userId);

        // カウントアップ
        Long count = redisTemplate.opsForValue().increment(key);

        // 初回失敗の場合、カウント窓を設定する
        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }

        // 閾値判定
        return count != null && count >= maxAttempts;
    }

    /**
     * 失敗カウントをリセットする。
     *
     * @param userId ユーザー ID
     */
    void reset(UUID userId) {
        redisTemplate.delete(REDIS_KEY_FAIL.formatted(userId));
    }
}
