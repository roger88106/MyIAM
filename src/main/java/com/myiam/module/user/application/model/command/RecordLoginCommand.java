package com.myiam.module.user.application.model.command;

import java.util.UUID;

/**
 * ログイン記録コマンド
 *
 * @param userId ユーザー ID
 */
public record RecordLoginCommand(UUID userId) {
}
