package com.myiam.module.identity.presentation.model.request;

import com.myiam.module.identity.presentation.validation.ValidEmail;
import com.myiam.module.identity.presentation.validation.ValidPassword;
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
        @ValidEmail
        String email,

        @NotEmpty
        @ValidPassword
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
