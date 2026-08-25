package com.myiam.module.user.presentation.model.response;

import java.time.Instant;
import java.util.UUID;

/**
 * ユーザーレスポンス
 *
 * @param userId      ユーザーID
 * @param email       メールアドレス
 * @param familyName  姓
 * @param givenName   名
 * @param lastLoginAt 最終ログイン日時
 */
public record UserResponse(UUID userId, String email, String familyName, String givenName, Instant lastLoginAt) {
}
