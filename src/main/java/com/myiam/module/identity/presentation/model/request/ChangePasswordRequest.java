package com.myiam.module.identity.presentation.model.request;

import com.myiam.module.identity.presentation.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * パスワード変更リクエスト
 *
 * @param oldPassword 古いパスワード
 * @param newPassword 新しいパスワード
 */
public record ChangePasswordRequest(
        @NotBlank
        @ValidPassword
        String oldPassword,

        @NotBlank
        @ValidPassword
        String newPassword) {
}
