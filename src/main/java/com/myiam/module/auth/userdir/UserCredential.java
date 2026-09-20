package com.myiam.module.auth.userdir;

import java.time.Instant;
import java.util.UUID;

/**
 * ユーザー認証情報
 *
 * @param userId            ユーザー ID
 * @param password          パスワード
 * @param enabled           アカウント有効
 * @param passwordLocked    パスワードロック中
 * @param passwordChangedAt パスワード変更日時
 */
public record UserCredential(UUID userId, String password, boolean enabled, boolean passwordLocked,
                             Instant passwordChangedAt) {
}
