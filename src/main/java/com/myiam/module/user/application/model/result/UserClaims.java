package com.myiam.module.user.application.model.result;

import lombok.Builder;

import java.util.UUID;

/**
 * ユーザークレーム
 *
 * @param userId ユーザー ID
 * @param email メールアドレス
 * @param familyName 姓
 * @param givenName 名
 */
@Builder
public record UserClaims(UUID userId, String email, String familyName, String givenName) {
}
