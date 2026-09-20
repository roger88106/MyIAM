package com.myiam.module.auth.config.bean;

import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * 認証認可モジュール専用のキャッシュマネージャー。
 */
public class AuthCacheManager extends CaffeineCacheManager {
}
