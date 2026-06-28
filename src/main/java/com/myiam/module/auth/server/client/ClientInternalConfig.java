package com.myiam.module.auth.server.client;

import org.jooq.DSLContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Objects;

import static com.myiam.module.auth.shared.constant.CacheNameConst.REGISTERED_CLIENT;
import static com.myiam.module.auth.shared.constant.CacheNameConst.REGISTERED_CLIENT_ID_MAP;

/**
 * クライアント管理の内部構成クラス。
 */
@Configuration
class ClientInternalConfig {

    /**
     * クライアント情報を管理するリポジトリ。<br />
     * DB上の「oauth2_registered_client」テーブルを管理する。<br />
     * 認可サービスのホットポイント、性能対策として キャッシュデコレータ でラップする。
     *
     * @param dsl jOOQ DSLContext
     * @param cacheManager CacheManager
     * @return ラップされた RegisteredClientRepository
     */
    @Bean
    RegisteredClientRepository registeredClientRepository(DSLContext dsl, CacheManager cacheManager) {
        // RDB にアクセスするリポジトリ
        RegisteredClientRepository jooqRepository = new JooqRegisteredClientRepository(dsl);

        // キャッシュ取得
        Cache clientCache = Objects.requireNonNull(cacheManager.getCache(REGISTERED_CLIENT),
                "Cache '%s' not found".formatted(REGISTERED_CLIENT));
        Cache clientIdMapCache = Objects.requireNonNull(cacheManager.getCache(REGISTERED_CLIENT_ID_MAP),
                "Cache '%s' not found".formatted(REGISTERED_CLIENT_ID_MAP));
        // リポジトリをデコレータでラップする
        return new CachingRegisteredClientRepository(
                jooqRepository,
                clientCache,
                clientIdMapCache
        );
    }
}
