package com.myiam.module.user.application.model.command;

import lombok.Builder;

import java.util.UUID;

/**
 * プロファイル更新コマンド
 *
 * @param userId ユーザー ID
 * @param familyName ユーザーの姓
 * @param givenName ユーザーの名前
 */
@Builder
public record UpdateProfileCommand(UUID userId, String familyName, String givenName) {
}
