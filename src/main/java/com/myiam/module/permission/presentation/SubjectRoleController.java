package com.myiam.module.permission.presentation;

import com.myiam.module.permission.application.PermissionCommandService;
import com.myiam.module.permission.application.PermissionQueryService;
import com.myiam.module.permission.application.model.command.AssignRoleCommand;
import com.myiam.module.permission.application.model.command.RevokeRoleCommand;
import com.myiam.module.permission.presentation.model.response.SubjectPermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * サブジェクトロールコントローラー
 */
@RestController
@RequestMapping("/api/subjects/{subject}")
@RequiredArgsConstructor
class SubjectRoleController {

    /**
     * 権限コマンドサービス
     */
    private final PermissionCommandService commandService;

    /**
     * 権限クエリーサービス
     */
    private final PermissionQueryService queryService;

    /**
     * サブジェクトロールマッパー
     */
    private final SubjectRoleMapper mapper;

// ============================== GET ==============================

    /**
     * サブジェクト権限取得
     *
     * @param subject サブジェクト
     * @return Http 200 : {@link SubjectPermissionResponse}
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<SubjectPermissionResponse> getPermissions(@PathVariable String subject) {
        // 権限取得
        var view = queryService.getPermissionByUserSubject(subject);

        // レスポンス設定
        return ResponseEntity.ok(mapper.toSubjectPermissionResponse(view));
    }

// ============================== PUT ==============================

    /**
     * ロール割当
     *
     * @param subject サブジェクト
     * @param role    ロール
     * @return Http 204 : VOID
     */
    @PutMapping("/roles/{role}")
    @PreAuthorize("hasAuthority('role:assign')")
    public ResponseEntity<Void> assignRole(@PathVariable String subject, @PathVariable String role) {
        // ロール割当
        commandService.assignRole(new AssignRoleCommand(subject, role));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }

// ============================== DELETE ==============================

    /**
     * ロール解除
     *
     * @param subject サブジェクト
     * @param role    ロール
     * @return Http 204 : VOID
     */
    @DeleteMapping("/roles/{role}")
    @PreAuthorize("hasAuthority('role:assign')")
    public ResponseEntity<Void> revokeRole(@PathVariable String subject, @PathVariable String role) {
        // ロール解除
        commandService.revokeRole(new RevokeRoleCommand(subject, role));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }
}
