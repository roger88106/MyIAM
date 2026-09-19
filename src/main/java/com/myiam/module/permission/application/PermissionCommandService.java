package com.myiam.module.permission.application;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.module.permission.application.model.command.AssignRoleCommand;
import com.myiam.module.permission.application.model.command.RevokeRoleCommand;
import com.myiam.module.permission.domain.subjectroles.PermissionErrorCode;
import com.myiam.module.permission.domain.subjectroles.port.RoleLookup;
import com.myiam.module.permission.domain.subjectroles.port.SubjectRolesRepository;
import com.myiam.module.permission.domain.subjectroles.vo.RoleRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 権限コマンドサービス
 */
@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    /**
     * 新規サブジェクトに割り当てるデフォルトロール
     */
    // ToDo: 現状はハードコード。テナントや登録経路ごとに変えたくなった時点で設定（yml）に外出しする
    private static final String DEFAULT_ROLE = "USER";

    /**
     * サブジェクトロールリポジトリ
     */
    private final SubjectRolesRepository subjectRolesRepository;

    /**
     * ロール検索
     */
    private final RoleLookup roleLookup;

    /**
     * ロール割当
     *
     * @param command ロール割当コマンド
     */
    @Transactional
    public void assignRole(AssignRoleCommand command) {
        // ロール取得
        RoleRef role = findEnabledRole(command.role());

        // サブジェクトロール取得
        var subjectRoles = subjectRolesRepository.findBySubject(command.subject());

        // ロール割当
        subjectRoles.assign(role);

        // サブジェクトロール保存
        subjectRolesRepository.save(subjectRoles);
    }

    /**
     * ロール解除
     *
     * @param command ロール解除コマンド
     */
    @Transactional
    public void revokeRole(RevokeRoleCommand command) {
        // ロール取得 ※停用済みロールの割当も解除できるよう、有効・無効を問わない
        RoleRef role = roleLookup.find(command.role())
                .orElseThrow(() -> BusinessException.of(PermissionErrorCode.ROLE_NOT_FOUND, List.of(command.role())));

        // サブジェクトロール取得
        var subjectRoles = subjectRolesRepository.findBySubject(command.subject());

        // ロール解除
        subjectRoles.revoke(role);

        // サブジェクトロール保存
        subjectRolesRepository.save(subjectRoles);
    }

    /**
     * デフォルトロール割当
     *
     * @param subject サブジェクト
     */
    @Transactional
    public void assignDefaultRole(String subject) {
        assignRole(new AssignRoleCommand(subject, DEFAULT_ROLE));
    }

// ============================== プライベートメソッド ==============================

    /**
     * 有効なロールを取得する
     *
     * @param role ロール
     * @return ロール参照
     */
    private RoleRef findEnabledRole(String role) {
        return roleLookup.findEnabled(role)
                .orElseThrow(() -> BusinessException.of(PermissionErrorCode.ROLE_NOT_FOUND, List.of(role)));
    }
}
