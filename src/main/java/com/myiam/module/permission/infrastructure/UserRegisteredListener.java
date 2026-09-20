package com.myiam.module.permission.infrastructure;

import com.myiam.module.identity.event.UserRegistered;
import com.myiam.module.permission.application.PermissionCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * ユーザー登録イベントのリスナー。<br />
 * 新規ユーザーにデフォルトロールを割り当てる。
 */
@Component
@RequiredArgsConstructor
class UserRegisteredListener {

    /**
     * 権限コマンドサービス
     */
    private final PermissionCommandService permissionCommandService;

    /**
     * ユーザー登録イベントを処理する
     *
     * @param event ユーザー登録イベント
     */
    @ApplicationModuleListener
    void on(UserRegistered event) {
        permissionCommandService.assignDefaultRole(event.userId().toString());
    }
}
