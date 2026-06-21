package com.myiam.module.user.presentation.model.request;

/**
 * プロフィール更新リクエスト
 *
 * @param familyName 姓
 * @param givenName 名
 */
public record UpdateProfileRequest(
        String familyName,
        String givenName
) {
}
