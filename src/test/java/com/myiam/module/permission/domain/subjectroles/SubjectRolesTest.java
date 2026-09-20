package com.myiam.module.permission.domain.subjectroles;

import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import com.myiam.module.permission.event.RoleAssigned;
import com.myiam.module.permission.event.RoleRevoked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SubjectRoles} の単体テスト。
 */
class SubjectRolesTest {

    private static final String SUBJECT = "subject-1";
    private static final RoleRef ADMIN = new RoleRef(UUID.randomUUID(), "ADMIN");
    private static final RoleRef VIEWER = new RoleRef(UUID.randomUUID(), "VIEWER");

    @Test
    @DisplayName("empty はロール未割当で生成される")
    void emptyHasNoRoles() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);

        assertThat(roles.subject()).isEqualTo(SUBJECT);
        assertThat(roles.roles()).isEmpty();
        assertThat(roles.pullEvents()).isEmpty();
    }

    @Test
    @DisplayName("assign するとロールが追加され RoleAssigned が積まれる")
    void assignAddsRoleWithEvent() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);

        roles.assign(ADMIN);

        assertThat(roles.roles()).containsExactly(ADMIN);
        assertThat(roles.pullEvents())
                .singleElement()
                .isInstanceOfSatisfying(RoleAssigned.class, e -> {
                    assertThat(e.subject()).isEqualTo(SUBJECT);
                    assertThat(e.role()).isEqualTo("ADMIN");
                });
    }

    @Test
    @DisplayName("assign は冪等：同じロールを二回割り当ててもイベントは一回")
    void assignIsIdempotent() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);
        roles.assign(ADMIN);
        roles.pullEvents();

        roles.assign(ADMIN);

        assertThat(roles.roles()).containsExactly(ADMIN);
        assertThat(roles.pullEvents()).isEmpty();
    }

    @Test
    @DisplayName("revoke するとロールが外れ RoleRevoked が積まれる")
    void revokeRemovesRoleWithEvent() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);
        roles.assign(ADMIN);
        roles.assign(VIEWER);
        roles.pullEvents();

        roles.revoke(ADMIN);

        assertThat(roles.roles()).containsExactly(VIEWER);
        assertThat(roles.pullEvents())
                .singleElement()
                .isInstanceOfSatisfying(RoleRevoked.class, e -> assertThat(e.role()).isEqualTo("ADMIN"));
    }

    @Test
    @DisplayName("revoke は冪等：割り当てられていないロールの解除は何もしない")
    void revokeIsIdempotent() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);

        roles.revoke(ADMIN);

        assertThat(roles.roles()).isEmpty();
        assertThat(roles.pullEvents()).isEmpty();
    }

    @Test
    @DisplayName("roles() は変更不可のコピーを返す")
    void rolesIsUnmodifiableCopy() {
        SubjectRoles roles = SubjectRoles.empty(SUBJECT);
        roles.assign(ADMIN);

        Set<RoleRef> view = roles.roles();

        assertThat(view).containsExactly(ADMIN);
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> view.add(VIEWER));
        assertThat(roles.roles()).containsExactly(ADMIN);
    }

    @Test
    @DisplayName("スナップショット → 復元 が往復できる")
    void snapshotRoundTrips() {
        SubjectRoles original = SubjectRoles.empty(SUBJECT);
        original.assign(ADMIN);
        original.assign(VIEWER);

        SubjectRoles restored = new SubjectRolesFactory().restore(original.toSnapshot());

        assertThat(restored.toSnapshot()).isEqualTo(original.toSnapshot());
        assertThat(restored.pullEvents()).isEmpty();
    }
}
