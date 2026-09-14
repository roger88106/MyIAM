package com.myiam.module.permission.api;

import com.myiam.module.permission.application.model.view.SubjectPermissionView;
import org.mapstruct.Mapper;

/**
 * 権限APIマッパー
 */
@Mapper(componentModel = "spring")
interface PermissionApiMapper {

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
