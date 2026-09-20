package com.myiam.module.permission.presentation;

import com.myiam.module.permission.application.model.view.SubjectPermissionView;
import com.myiam.module.permission.presentation.model.response.SubjectPermissionResponse;
import org.mapstruct.Mapper;

/**
 * サブジェクトロールマッパー
 */
@Mapper(componentModel = "spring")
interface SubjectRoleMapper {

// ============================== MapStruct 変換 ==============================

    /**
     * {@link SubjectPermissionView} を {@link SubjectPermissionResponse} に変換する
     *
     * @param view 変換する {@link SubjectPermissionView}
     * @return {@link SubjectPermissionResponse}
     */
    SubjectPermissionResponse toSubjectPermissionResponse(SubjectPermissionView view);

// ============================== デフォルトメソッド変換 ==============================

}
