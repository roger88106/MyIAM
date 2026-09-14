package com.myiam.module.permission.infrastructure;

import com.myiam.module.permission.application.PermissionQueryRepository;
import com.myiam.module.permission.application.model.view.SubjectPermissionView;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.myiam.jooq.permission.Tables.*;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;


/**
 * 権限クエリリポジトリ
 */
@Repository
@RequiredArgsConstructor
class JooqPermissionQueryRepository implements PermissionQueryRepository {

    /**
     * jOOQ DSLコンテキスト
     */
    private final DSLContext dsl;

    /**
     * サブジェクトの権限を取得する
     *
     * @param subject サブジェクト
     * @return サブジェクトの権限ビュー
     */
    public Optional<SubjectPermissionView> findPermissionByUserSubject(String subject) {
        // ロールのマルチセットクエリ
        Field<List<String>> rolesField = multiset(
                select(ROLES.ROLE)
                        .from(SUBJECTS_ROLES)
                        .join(ROLES).on(ROLES.ID.eq(SUBJECTS_ROLES.ROLE_ID))
                        .where(SUBJECTS_ROLES.SUBJECT.eq(subject))
                        .and(ROLES.ENABLED)
        ).convertFrom(r -> r.map(rec -> rec.get(ROLES.ROLE)));

        // 権限のマルチセットクエリ
        Field<List<String>> permissionsField = multiset(
                select(PERMISSIONS.PERMISSION)
                        .from(SUBJECTS_ROLES)
                        .join(ROLES).on(ROLES.ID.eq(SUBJECTS_ROLES.ROLE_ID))
                        .join(ROLES_PERMISSIONS).on(ROLES_PERMISSIONS.ROLE_ID.eq(ROLES.ID))
                        .join(PERMISSIONS).on(PERMISSIONS.ID.eq(ROLES_PERMISSIONS.PERMISSION_ID))
                        .where(SUBJECTS_ROLES.SUBJECT.eq(subject))
                        .and(ROLES.ENABLED)
        ).convertFrom(r -> r.map(rec -> rec.get(PERMISSIONS.PERMISSION)));

        // 検索実行
        return dsl.select(rolesField, permissionsField)
                // ※現時点ではOptionalが空になるパターンはなし。
                .fetchOptional(r -> SubjectPermissionView
                        .builder()
                        .subject(subject)
                        .roles(new HashSet<>(r.get(rolesField)))
                        .permissions(new HashSet<>(r.get(permissionsField)))
                        .build()
                );
    }

}
