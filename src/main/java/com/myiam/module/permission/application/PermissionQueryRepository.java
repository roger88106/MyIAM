package com.myiam.module.permission.application;

import com.myiam.module.permission.application.model.view.SubjectPermissionView;

import java.util.Optional;

/**
 * 権限クエリリポジトリ
 */
public interface PermissionQueryRepository {

    /**
     * サブジェクトの権限を取得する
     *
     * @param subject サブジェクト
     * @return サブジェクトの権限ビュー
     */
    Optional<SubjectPermissionView> findPermissionByUserSubject(String subject);
}
