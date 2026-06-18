package com.myiam.module.user.application.model.command;

import lombok.Builder;

/**
 * ユーザー登録コマンド
 *
 * @param username ユーザー名
 * @param email メールアドレス
 * @param password パスワード
 * @param profile プロフィール
 */
@Builder
public record RegisterUserCommand(String username, String email, String password, Profile profile) {

    /**
     * プロフィール
     * @param familyName 姓
     * @param givenName 名
     */
    public record Profile(String familyName, String givenName) {
    }

}
