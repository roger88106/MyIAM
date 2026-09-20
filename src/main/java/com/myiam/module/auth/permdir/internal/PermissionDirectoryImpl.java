package com.myiam.module.auth.permdir.internal;

import com.myiam.module.auth.permdir.PermissionDirectory;
import com.myiam.module.auth.permdir.SubjectPermissions;
import com.myiam.module.permission.api.PermissionApi;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.myiam.module.auth.shared.constant.CacheNameConst.SUBJECT_PERMISSIONS;

/**
 * 権限ディレクトリ実装
 */
@Component
@RequiredArgsConstructor
class PermissionDirectoryImpl implements PermissionDirectory {

    /**
     * 権限API
     */
    private final PermissionApi permissionApi;

    /**
     * 権限ディレクトリマッパー
     */
    private final PermissionDirectoryMapper mapper;

    /**
     * サブジェクトの権限情報を取得する
     *
     * @param subject サブジェクト
     * @return サブジェクト権限情報
     */
    @Override
    @Cacheable(cacheNames = SUBJECT_PERMISSIONS, key = "#subject", unless = "#result == null")
    public Optional<SubjectPermissions> findPermissions(String subject) {
        // 権限情報取得
        var permissions = mapper.toSubjectPermissions(
                permissionApi.getPermissionBySubject(subject));

        // Optionalに包んで返す
        // ※今のapiの実装はnullしないため、"of"を使用し、nullの場合NPEを返す
        return Optional.of(permissions);
    }
}
