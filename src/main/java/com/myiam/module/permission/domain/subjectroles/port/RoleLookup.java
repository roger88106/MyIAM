package com.myiam.module.permission.domain.subjectroles.port;

import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;

import java.util.Optional;

/**
 * ロール検索ポート。<br />
 * ロール名から有効なロールの参照を引く。
 */
public interface RoleLookup {

    /**
     * ロールを検索する ※有効・無効を問わない
     *
     * @param role ロール
     * @return ロール参照
     */
    Optional<RoleRef> find(String role);

    /**
     * 有効なロールを検索する
     *
     * @param role ロール
     * @return ロール参照
     */
    Optional<RoleRef> findEnabled(String role);
}
