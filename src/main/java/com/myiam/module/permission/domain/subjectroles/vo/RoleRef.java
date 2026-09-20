package com.myiam.module.permission.domain.subjectroles.vo;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * ロール参照
 *
 * @param id   ロールID
 * @param role ロール
 */
@ValueObject
public record RoleRef(UUID id, String role) {

    /**
     * ロール参照
     *
     * @param id   ロールID
     * @param role ロール
     */
    public RoleRef {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
