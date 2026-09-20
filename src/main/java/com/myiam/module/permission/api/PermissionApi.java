package com.myiam.module.permission.api;

import com.myiam.module.permission.application.PermissionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 権限API
 */
@Component
@RequiredArgsConstructor
public class PermissionApi {

    /**
     * 権限クエリーサービス
     */
    private final PermissionQueryService permissionQueryService;
    /**
     * 権限マッパー
     */
    private final PermissionApiMapper mapper;

    /**
     * 指定したサブジェクトの権限情報を取得する
     *
     * @param subject サブジェクト
     * @return サブジェクト権限レスポンス
     */
    public SubjectPermissionResponse getPermissionBySubject(String subject) {
        // 権限情報取得
        var permission = permissionQueryService.getPermissionByUserSubject(subject);

        // レスポンスを返す
        return mapper.toSubjectPermissionResponse(permission);
    }
}