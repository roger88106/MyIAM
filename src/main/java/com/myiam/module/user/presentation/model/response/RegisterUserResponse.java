package com.myiam.module.user.presentation.model.response;

import java.util.UUID;

/**
 * ユーザー登録レスポンス
 *
 * @param userId ユーザー ID
 */
public record RegisterUserResponse(UUID userId) {
}
