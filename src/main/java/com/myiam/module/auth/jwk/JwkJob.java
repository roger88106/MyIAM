package com.myiam.module.auth.jwk;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * JWK ジョブクラス
 */
@Component
@RequiredArgsConstructor
class JwkJob {

    /**
     * JWK 管理サービス
     */
    private final JwkManagerService jwkManagerService;

    /**
     * 初期化処理実行
     */
    @EventListener(ContextRefreshedEvent.class)
    void runInit() {
        jwkManagerService.init();
    }

    /**
     * キーローテーション実行処理。<br />
     * 新しい署名鍵を生成
     */
    @Scheduled(cron = "${jwt.key.rotation.cron}")
    void rotateKeys() {
        // 新しい署名用鍵を生成して保存
        jwkManagerService.jwkRotation();
    }

}
