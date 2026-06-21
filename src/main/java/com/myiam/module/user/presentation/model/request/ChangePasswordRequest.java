package com.myiam.module.user.presentation.model.request;

/**
 * パスワード変更リクエスト
 *
 * @param oldPassword 古いパスワード
 * @param newPassword 新しいパスワード
 */
public record ChangePasswordRequest(
        String oldPassword,
        String newPassword) {
}
