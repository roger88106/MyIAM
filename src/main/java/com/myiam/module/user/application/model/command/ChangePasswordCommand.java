package com.myiam.module.user.application.model.command;

import lombok.Builder;

import java.util.UUID;

/**
 * パスワード変更コマンド
 *
 * @param userId ユーザー ID
 * @param oldPassword 古いパスワード
 * @param newPassword 新しいパスワード
 */
@Builder
public record ChangePasswordCommand(UUID userId, String oldPassword, String newPassword) {
}
