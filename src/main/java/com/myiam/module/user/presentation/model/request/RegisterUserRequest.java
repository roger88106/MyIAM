package com.myiam.module.user.presentation.model.request;

import com.myiam.module.user.presentation.validation.Email;
import com.myiam.module.user.presentation.validation.Password;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * ユーザー登録リクエスト
 *
 * @param email    メールアドレス
 * @param password パスワード
 * @param profile  プロファイル
 */
public record RegisterUserRequest(
        @NotEmpty
        @Email
        String email,

        @NotEmpty
        @Password
        String password,

        @Valid
        @NotNull
        Profile profile
) {

    /**
     * プロファイル
     *
     * @param familyName 姓
     * @param givenName  名
     */
    public record Profile(
            @NotEmpty
            String familyName,

            @NotEmpty
            String givenName) {
    }
}
