package com.myiam.module.user.domain.user;

import com.myiam.common.message.I18nMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ユーザー関連のメッセージ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMessage {
    /**
     * パスワードが一致していない。
     */
    public static final I18nMessage INVALID_PASSWORD = I18nMessage.of(Code.INVALID_PASSWORD);

    /**
     * ユーザーが存在しない。
     */
    public static final I18nMessage USER_NOT_FOUND = I18nMessage.of(Code.USER_NOT_FOUND);

    /**
     * ユーザー {username} は既に存在しています。
     */
    public static final I18nMessage USER_ALREADY_EXISTS = I18nMessage.of(Code.USER_ALREADY_EXISTS, List.of("username"));

    /**
     * ユーザーは既に無効化されています。（ID: {userId}）
     */
    public static final I18nMessage USER_ALREADY_DISABLED = I18nMessage.of(Code.USER_ALREADY_DISABLED, List.of("userId"));

    // ============================== コード定義 ==============================

    /**
     * メッセージコード
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Code{
        /**
         * パスワードが一致していない。
         */
        public static final String INVALID_PASSWORD = "myiam.user.invalid_password";

        /**
         * ユーザー {username} は既に存在しています。
         */
        public static final String USER_NOT_FOUND = "myiam.user.user_not_found";

        /**
         * ユーザー {username} は既に存在しています。
         */
        public static final String USER_ALREADY_EXISTS = "myiam.user.user_already_exists";

        /**
         * ユーザーは既に無効化されています。（ID: {userId}）
         */
        public static final String USER_ALREADY_DISABLED = "myiam.user.user_already_disabled";
    }
}
