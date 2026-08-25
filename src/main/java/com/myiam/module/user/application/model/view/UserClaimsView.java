package com.myiam.module.user.application.model.view;

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
public record UserClaimsView(UUID userId, String email, String familyName, String givenName) {
}
