package com.myiam.module.permission.infrastructure;

import com.myiam.module.permission.domain.subjectroles.SubjectRoles;
import com.myiam.module.permission.domain.subjectroles.SubjectRolesFactory;
import com.myiam.module.permission.domain.subjectroles.port.SubjectRolesRepository;
import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.myiam.jooq.permission.Tables.ROLES;
import static com.myiam.jooq.permission.Tables.SUBJECTS_ROLES;

/**
 * サブジェクトロールのリポジトリ
 */
@Repository
@RequiredArgsConstructor
class JooqSubjectRolesRepository implements SubjectRolesRepository {

    /**
     * jOOQ DSL Context
     */
    private final DSLContext dsl;

    /**
     * Spring イベントパブリッシャー
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * サブジェクトロールファクトリ
     */
    private final SubjectRolesFactory factory;

    /**
     * サブジェクトロール取得
     *
     * @param subject サブジェクト
     * @return サブジェクトロール
     */
    @Override
    public SubjectRoles findBySubject(String subject) {
        // 割当済みロールを取得
        Set<RoleRef> roles = new HashSet<>(dsl.select(ROLES.ID, ROLES.ROLE)
                .from(SUBJECTS_ROLES)
                .join(ROLES).on(ROLES.ID.eq(SUBJECTS_ROLES.ROLE_ID))
                .where(SUBJECTS_ROLES.SUBJECT.eq(subject))
                .fetch(r -> new RoleRef(r.get(ROLES.ID), r.get(ROLES.ROLE))));

        // スナップショットからドメインに変換する
        return factory.restore(new SubjectRoles.Snapshot(subject, roles));
    }

    /**
     * サブジェクトロール保存。<br />
     * ※差分更新：スナップショットに無い割当は削除し、新しい割当のみ追加する。
     *
     * @param subjectRoles サブジェクトロール
     */
    @Override
    public void save(SubjectRoles subjectRoles) {
        // スナップショット取得
        var snapshot = subjectRoles.toSnapshot();
        Set<UUID> roleIds = snapshot.roles().stream()
                .map(RoleRef::id)
                .collect(Collectors.toSet());

        // スナップショットに無い割当を削除
        var delete = dsl.deleteFrom(SUBJECTS_ROLES)
                .where(SUBJECTS_ROLES.SUBJECT.eq(snapshot.subject()));
        if (!roleIds.isEmpty()) {
            delete = delete.and(SUBJECTS_ROLES.ROLE_ID.notIn(roleIds));
        }
        delete.execute();

        // 新しい割当を追加 ※既存はスキップ
        roleIds.forEach(roleId -> dsl.insertInto(SUBJECTS_ROLES)
                .set(SUBJECTS_ROLES.SUBJECT, snapshot.subject())
                .set(SUBJECTS_ROLES.ROLE_ID, roleId)
                .onConflictDoNothing()
                .execute());

        // イベント発行
        subjectRoles.pullEvents().forEach(eventPublisher::publishEvent);
    }
}
