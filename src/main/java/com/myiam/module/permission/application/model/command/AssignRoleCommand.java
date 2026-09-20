package com.myiam.module.permission.application.model.command;

/**
 * ロール割当コマンド
 *
 * @param subject サブジェクト
 * @param role    ロール
 */
public record AssignRoleCommand(String subject, String role) {
}
