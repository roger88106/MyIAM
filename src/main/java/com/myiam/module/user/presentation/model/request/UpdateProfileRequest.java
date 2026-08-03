package com.myiam.module.user.presentation.model.request;

import jakarta.validation.constraints.NotEmpty;

/**
 * プロフィール更新リクエスト
 *
 * @param familyName 姓
 * @param givenName 名
 */
public record UpdateProfileRequest(
        @NotEmpty
        String familyName,

        @NotEmpty
        String givenName) {
}
