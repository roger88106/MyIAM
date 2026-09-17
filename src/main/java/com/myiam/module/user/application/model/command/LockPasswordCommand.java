package com.myiam.module.user.application.model.command;

import java.util.UUID;

/**
 * パスワードロックコマンド
 *
 * @param userId ユーザー ID
 */
public record LockPasswordCommand(UUID userId) {
}
