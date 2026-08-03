package com.myiam.module.user.presentation.model.request;

import com.myiam.module.user.presentation.validation.Password;
import jakarta.validation.constraints.NotBlank;

/**
 * パスワード変更リクエスト
 *
 * @param oldPassword 古いパスワード
 * @param newPassword 新しいパスワード
 */
public record ChangePasswordRequest(
        @NotBlank
        @Password
        String oldPassword,

        @NotBlank
        @Password
        String newPassword) {
}
