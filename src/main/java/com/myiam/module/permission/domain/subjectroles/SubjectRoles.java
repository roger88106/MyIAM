package com.myiam.module.permission.domain.subjectroles;

import com.myiam.common.model.domain.AggregateRoot;
import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import com.myiam.module.permission.event.PermissionEvent;
import com.myiam.module.permission.event.RoleAssigned;
import com.myiam.module.permission.event.RoleRevoked;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jmolecules.ddd.annotation.Identity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * サブジェクトロールの Aggregate Root。<br />
 * 一つのサブジェクトに割り当てられたロールの集合。
 */
@org.jmolecules.ddd.annotation.AggregateRoot
@EqualsAndHashCode(of = "subject", callSuper = false)
public class SubjectRoles extends AggregateRoot {

// ============================== フィールド定義 ==============================

    /**
     * サブジェクト
     */
    @Identity
    private final String subject;

    /**
     * 割当済みロール
     */
    private final Set<RoleRef> roles;

// ============================== コンストラクタ ==============================

    /**
     * サブジェクトロールの全属性コンストラクタ
     *
     * @param subject サブジェクト
     * @param roles   割当済みロール
     */
    private SubjectRoles(String subject, Set<RoleRef> roles) {
        this.subject = Objects.requireNonNull(subject, "subject must not be null");
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "roles must not be null"));
    }

// ============================== Getter ==============================

    /**
     * @return サブジェクト
     */
    public String subject() {
        return subject;
    }

    /**
     * @return 割当済みロール（変更不可）
     */
    public Set<RoleRef> roles() {
        return Set.copyOf(roles);
    }

// ============================== ドメインの振る舞い ==============================

    /**
     * ロール未割当のサブジェクトロールを作成する
     *
     * @param subject サブジェクト
     * @return サブジェクトロール
     */
    public static SubjectRoles empty(@NonNull String subject) {
        return new SubjectRoles(subject, Set.of());
    }

    /**
     * ロールを割り当てる。<br />
     * ※既に割当済みの場合は何もしない。
     *
     * @param role ロール
     */
    public void assign(@NonNull RoleRef role) {
        // 既に割当済みの場合、何もしない
        if (!roles.add(role)) {
            return;
        }

        // イベント登録：ロールが割り当てられた
        registerEvent(RoleAssigned.of(subject, role.role()));
    }

    /**
     * ロールを解除する。<br />
     * ※割り当てられていない場合は何もしない。
     *
     * @param role ロール
     */
    public void revoke(@NonNull RoleRef role) {
        // 割り当てられていない場合、何もしない
        if (!roles.remove(role)) {
            return;
        }

        // イベント登録：ロールが解除された
        registerEvent(RoleRevoked.of(subject, role.role()));
    }

// ============================== ドメインイベント関連処理 ==============================

    /**
     * ドメインイベントを登録する
     *
     * @param event 権限ドメインのイベント
     */
    void registerEvent(PermissionEvent event) {
        super.registerEvent(event);
    }

// ============================== スナップショット関連処理 ==============================

    /**
     * サブジェクトロールスナップショット
     *
     * @param subject サブジェクト
     * @param roles   割当済みロール
     */
    public record Snapshot(String subject, Set<RoleRef> roles) {
    }

    /**
     * スナップショットを取得する
     *
     * @return サブジェクトロールスナップショット
     */
    public Snapshot toSnapshot() {
        return new Snapshot(subject, Set.copyOf(roles));
    }

    /**
     * スナップショットから復元する
     *
     * @param snapshot サブジェクトロールスナップショット
     */
    SubjectRoles(Snapshot snapshot) {
        this(snapshot.subject(), snapshot.roles());
    }
}
