package com.myiam.module.auth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.myiam.module.auth.shared.constant.CacheNameConst;
import lombok.Getter;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * キャッシュ構成クラス。
 */
@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "app.cache")
class CacheConfig {

    /**
     * キャッシュ設定マップ。<br />
     * application.yml の app.cache から取得する
     */
    @Getter
    private final Map<String, String> specs = new HashMap<>();

    /**
     * Caffeine キャッシュマネージャーの構成。
     *
     * @return キャッシュマネージャー
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // ソース中で定義したキャッシュ名を取得
        Set<String> allowedNames = new HashSet<>(CacheNameConst.CACHE_NAMES);

        // キャッシュを全件登録
        specs.forEach((cacheName, specString) -> {
            if (allowedNames.contains(cacheName)) {
                // 許可されたキャッシュ名の場合、キャッシュマネージャーに登録する
                cacheManager.registerCustomCache(cacheName, Caffeine.from(specString).build());
                // 登録済みのキャッシュ名をセットから削除する
                allowedNames.remove(cacheName);
            } else {
                // 未定義のキャッシュ名が指定された場合、Springの構成を中止する
                throw new BeanInitializationException(String.format(
                        "【キャッシュ構成エラー】application.yml で定義されたキャッシュ名 [%s] は、CacheName クラスに定義されていません。",
                        cacheName
                ));
            }
        });

        // 登録漏れのキャッシュ名が存在した場合、Springの構成を中止する
        if (!allowedNames.isEmpty()) {
            throw new BeanInitializationException(String.format(
                    "【キャッシュ構成エラー】CacheName に定義されているキャッシュ [%s] の設定が application.yml に不足しています。",
                    String.join(",", allowedNames)
            ));
        }

        return cacheManager;
    }
}
