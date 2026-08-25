package com.myiam.module.user.application.model.view;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * ユーザー明細
 *
 * @param userId      ユーザーID
 * @param email       メールアドレス
 * @param familyName  姓
 * @param givenName   名
 * @param lastLoginAt 最終ログイン日時
 */
@Builder
public record UserDetailView(UUID userId, String email, String familyName, String givenName, Instant lastLoginAt) {
}
