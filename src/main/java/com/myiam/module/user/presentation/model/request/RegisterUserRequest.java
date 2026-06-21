package com.myiam.module.user.presentation.model.request;

import jakarta.validation.constraints.Email;

/**
 * ユーザー登録リクエスト
 *
 * @param username ユーザー名
 * @param email    メールアドレス
 * @param password パスワード
 * @param profile  プロファイル
 */
public record RegisterUserRequest(
        String username,
        @Email
        String email,
        String password,
        Profile profile
) {

    /**
     * プロファイル
     *
     * @param familyName 姓
     * @param givenName  名
     */
    public record Profile(String familyName, String givenName) {
    }
}
