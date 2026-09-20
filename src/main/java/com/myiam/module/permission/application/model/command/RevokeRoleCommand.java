package com.myiam.module.permission.application.model.command;

/**
 * ロール解除コマンド
 *
 * @param subject サブジェクト
 * @param role    ロール
 */
public record RevokeRoleCommand(String subject, String role) {
}
