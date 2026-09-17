package com.myiam.module.user.application.model.command;

import java.util.UUID;

/**
 * パスワードロック解除コマンド
 *
 * @param userId ユーザー ID
 */
public record UnlockPasswordCommand(UUID userId) {
}
