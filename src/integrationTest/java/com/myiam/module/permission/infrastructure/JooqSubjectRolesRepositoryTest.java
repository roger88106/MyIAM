package com.myiam.module.permission.infrastructure;

import com.myiam.TestcontainersConfig;
import com.myiam.module.permission.domain.subjectroles.SubjectRoles;
import com.myiam.module.permission.domain.subjectroles.port.RoleLookup;
import com.myiam.module.permission.domain.subjectroles.port.SubjectRolesRepository;
import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import com.myiam.module.permission.event.RoleAssigned;
import com.myiam.module.permission.event.RoleRevoked;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static com.myiam.jooq.permission.Tables.SUBJECTS_ROLES;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link JooqSubjectRolesRepository} の統合テスト。<br />
 * 各テストはトランザクション内で実行され、終了時にロールバックされる。
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@Transactional
@RecordApplicationEvents
class JooqSubjectRolesRepositoryTest {

    @Autowired
    SubjectRolesRepository repository;

    @Autowired
    RoleLookup roleLookup;

    @Autowired
    DSLContext dsl;

    @Autowired
    ApplicationEvents events;

    /** テストごとに一意なサブジェクト（seed データと衝突させない） */
    private String subject;

    private RoleRef admin;
    private RoleRef viewer;
    private RoleRef userManager;

    @BeforeEach
    void setUp() {
        subject = "it-" + UUID.randomUUID();
        admin = roleLookup.findEnabled("ADMIN").orElseThrow();
        viewer = roleLookup.findEnabled("VIEWER").orElseThrow();
        userManager = roleLookup.findEnabled("USER_MANAGER").orElseThrow();
    }

    // ============================== findBySubject ==============================

    @Test
    @DisplayName("割当が無いサブジェクトは空のロール集合で返る（null ではない）")
    void findBySubjectReturnsEmptyWhenNoAssignment() {
        SubjectRoles found = repository.findBySubject(subject);

        assertThat(found.subject()).isEqualTo(subject);
        assertThat(found.roles()).isEmpty();
    }

    @Test
    @DisplayName("割当済みのロールを id と名前付きで復元する")
    void findBySubjectRestoresAssignedRoles() {
        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(admin);
        roles.assign(viewer);
        repository.save(roles);

        SubjectRoles found = repository.findBySubject(subject);

        assertThat(found.roles()).containsExactlyInAnyOrder(admin, viewer);
    }

    // ============================== save：差分更新 ==============================

    @Test
    @DisplayName("save は差分更新：追加分だけ INSERT、消えた分だけ DELETE、残りは触らない")
    void saveAppliesDiff() {
        // 初期状態：ADMIN + VIEWER
        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(admin);
        roles.assign(viewer);
        repository.save(roles);

        // VIEWER を外して USER_MANAGER を足す
        SubjectRoles reloaded = repository.findBySubject(subject);
        reloaded.revoke(viewer);
        reloaded.assign(userManager);
        repository.save(reloaded);

        assertThat(rowsOf(subject)).containsExactlyInAnyOrder(admin.id(), userManager.id());
    }

    @Test
    @DisplayName("全ロールを解除して save すると行が全て消える")
    void saveWithNoRolesDeletesAllRows() {
        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(admin);
        repository.save(roles);

        roles.revoke(admin);
        repository.save(roles);

        assertThat(rowsOf(subject)).isEmpty();
    }

    @Test
    @DisplayName("変更が無い save は冪等（行数が変わらず、例外も出ない）")
    void saveIsIdempotentWhenUnchanged() {
        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(admin);
        repository.save(roles);

        repository.save(repository.findBySubject(subject));

        assertThat(rowsOf(subject)).containsExactly(admin.id());
    }

    @Test
    @DisplayName("他サブジェクトの割当には影響しない")
    void saveDoesNotTouchOtherSubjects() {
        String other = "it-other-" + UUID.randomUUID();
        SubjectRoles otherRoles = SubjectRoles.empty(other);
        otherRoles.assign(admin);
        repository.save(otherRoles);

        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(viewer);
        repository.save(roles);
        roles.revoke(viewer);
        repository.save(roles);

        assertThat(rowsOf(other)).containsExactly(admin.id());
    }

    // ============================== save：イベント発行 ==============================

    @Test
    @DisplayName("save でアグリゲートに溜まったイベントが発行され、発行後は空になる")
    void savePublishesPendingEvents() {
        SubjectRoles roles = SubjectRoles.empty(subject);
        roles.assign(admin);
        roles.assign(viewer);
        roles.revoke(viewer);
        repository.save(roles);

        assertThat(events.stream(RoleAssigned.class))
                .extracting(RoleAssigned::subject, RoleAssigned::role)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(subject, "ADMIN"),
                        org.assertj.core.groups.Tuple.tuple(subject, "VIEWER"));
        assertThat(events.stream(RoleRevoked.class))
                .extracting(RoleRevoked::role)
                .containsExactly("VIEWER");
        assertThat(roles.pullEvents()).isEmpty();
    }

    // ============================== ヘルパー ==============================

    private Set<UUID> rowsOf(String subject) {
        return dsl.select(SUBJECTS_ROLES.ROLE_ID)
                .from(SUBJECTS_ROLES)
                .where(SUBJECTS_ROLES.SUBJECT.eq(subject))
                .fetchSet(SUBJECTS_ROLES.ROLE_ID);
    }
}
