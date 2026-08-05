package com.myiam.module.auth.userdir.internal;

import com.myiam.module.auth.config.bean.AuthCacheManager;
import com.myiam.module.user.event.ProfileUpdated;
import org.springframework.cache.Cache;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

import static com.myiam.module.auth.shared.constant.CacheNameConst.USER_CLAIMS;

/**
 * ユーザーディレクトリ キャッシュマネージャー
 */
@Component
class UserDirectoryCacheManager {

    /**
     * ユーザークレームキャッシュ
     */
    private final Cache userClaimsCache;

    /**
     * {@link UserDirectoryCacheManager} のコンストラクタ
     *
     * @param cacheManager キャッシュマネージャー
     */
    UserDirectoryCacheManager(AuthCacheManager cacheManager) {
        this.userClaimsCache = Objects.requireNonNull(cacheManager.getCache(USER_CLAIMS), "Cache '%s' not found".formatted(USER_CLAIMS));
    }

    // ============================== イベントリスナー ==============================

    /**
     * ユーザーイベントのリスナー<br />
     * ユーザーイベントが発生した際に、キャッシュを削除する。
     *
     * @param event ユーザーイベント
     */
    @ApplicationModuleListener
    void on(ProfileUpdated event) {
        evictUserClaimsById(event.userId());
    }

    // ============================== プライベートメソッド ==============================

    /**
     * ユーザークレームキャッシュを削除する。
     *
     * @param userId ユーザー ID
     */
    private void evictUserClaimsById(UUID userId) {
        userClaimsCache.evict(userId);
    }

}
