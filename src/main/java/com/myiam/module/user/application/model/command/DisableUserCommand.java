package com.myiam.module.user.application.model.command;

import java.util.UUID;

/**
 * ユーザー無効化コマンド
 *
 * @param userId ユーザー ID
 */
public record DisableUserCommand(UUID userId) {
}
