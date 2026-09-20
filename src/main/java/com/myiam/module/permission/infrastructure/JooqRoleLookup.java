package com.myiam.module.permission.infrastructure;

import com.myiam.module.permission.domain.subjectroles.port.RoleLookup;
import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.myiam.jooq.permission.Tables.ROLES;

/**
 * ロール検索の jOOQ 実装
 */
@Repository
@RequiredArgsConstructor
class JooqRoleLookup implements RoleLookup {

    /**
     * jOOQ DSL Context
     */
    private final DSLContext dsl;

    /**
     * ロールを検索する
     *
     * @param role ロール
     * @return ロール参照
     */
    @Override
    public Optional<RoleRef> find(String role) {
        return dsl.select(ROLES.ID, ROLES.ROLE)
                .from(ROLES)
                .where(ROLES.ROLE.eq(role))
                .fetchOptional(r -> new RoleRef(r.get(ROLES.ID), r.get(ROLES.ROLE)));
    }

    /**
     * 有効なロールを検索する
     *
     * @param role ロール
     * @return ロール参照
     */
    @Override
    public Optional<RoleRef> findEnabled(String role) {
        return dsl.select(ROLES.ID, ROLES.ROLE)
                .from(ROLES)
                .where(ROLES.ROLE.eq(role))
                .and(ROLES.ENABLED)
                .fetchOptional(r -> new RoleRef(r.get(ROLES.ID), r.get(ROLES.ROLE)));
    }
}
