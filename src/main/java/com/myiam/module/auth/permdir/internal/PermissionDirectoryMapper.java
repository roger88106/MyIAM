package com.myiam.module.auth.permdir.internal;

import com.myiam.module.auth.permdir.SubjectPermissions;
import com.myiam.module.permission.api.SubjectPermissionResponse;
import org.mapstruct.Mapper;

/**
 * 権限ディレクトリマッパー
 */
@Mapper(componentModel = "spring")
interface PermissionDirectoryMapper {

    /**
     * {@link SubjectPermissionResponse} を {@link SubjectPermissions} に変換する
     *
     * @param response 変換する {@code SubjectPermissionResponse}
     * @return {@code SubjectPermissions}
     */
    SubjectPermissions toSubjectPermissions(SubjectPermissionResponse response);
}
