package com.myiam.module.auth.permdir.internal;

import com.myiam.module.auth.config.bean.AuthCacheManager;
import com.myiam.module.permission.event.RoleAssigned;
import com.myiam.module.permission.event.RoleRevoked;
import org.springframework.cache.Cache;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.myiam.module.auth.shared.constant.CacheNameConst.SUBJECT_PERMISSIONS;

/**
 * 権限ディレクトリ キャッシュマネージャー
 */
@Component
class PermissionDirectoryCacheManager {

    /**
     * サブジェクト権限キャッシュ
     */
    private final Cache subjectPermissionsCache;

    /**
     * {@link PermissionDirectoryCacheManager} のコンストラクタ
     *
     * @param cacheManager キャッシュマネージャー
     */
    PermissionDirectoryCacheManager(AuthCacheManager cacheManager) {
        this.subjectPermissionsCache = Objects.requireNonNull(cacheManager.getCache(SUBJECT_PERMISSIONS), "Cache '%s' not found".formatted(SUBJECT_PERMISSIONS));
    }

    // ============================== イベントリスナー ==============================

    /**
     * ロール割当イベントのリスナー
     *
     * @param event ロール割当イベント
     */
    @ApplicationModuleListener
    void on(RoleAssigned event) {
        evictBySubject(event.subject());
    }

    /**
     * ロール解除イベントのリスナー
     *
     * @param event ロール解除イベント
     */
    @ApplicationModuleListener
    void on(RoleRevoked event) {
        evictBySubject(event.subject());
    }

    // ============================== プライベートメソッド ==============================

    /**
     * サブジェクト権限キャッシュを削除する。
     *
     * @param subject サブジェクト
     */
    private void evictBySubject(String subject) {
        subjectPermissionsCache.evict(subject);
    }
}
