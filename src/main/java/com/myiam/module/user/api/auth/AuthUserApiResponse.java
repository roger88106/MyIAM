package com.myiam.module.user.api.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * 認証認可モジュール向けの ユーザーAPI レスポンス
 */
public class AuthUserApiResponse {

    /**
     * ユーザー認証情報
     *
     * @param userId            ユーザー ID
     * @param password          パスワード
     * @param enabled           アカウント有効
     * @param passwordLocked    パスワードロック中
     * @param passwordChangedAt パスワード変更日時
     */
    public record Credential(UUID userId, String password, boolean enabled, boolean passwordLocked,
                             Instant passwordChangedAt) {
    }

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
}
