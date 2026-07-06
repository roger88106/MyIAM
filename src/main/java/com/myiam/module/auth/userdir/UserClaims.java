package com.myiam.module.auth.userdir;

import java.util.UUID;

/**
 * ユーザークレーム
 *
 * @param userId     ユーザー ID
 * @param email      メールアドレス
 * @param familyName 姓
 * @param givenName  名
 */
public record UserClaims(UUID userId, String email, String familyName, String givenName) {
}
