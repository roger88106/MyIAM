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
    private static final String PREFIX = "myiam.user";

    /**
     * パスワードが一致していない。
     */
    public static final I18nMessage INVALID_PASSWORD = I18nMessage.of(PREFIX, "invalid_password");

    /**
     * ユーザーが存在しない。
     */
    public static final I18nMessage USER_NOT_FOUND = I18nMessage.of(PREFIX, "user_not_found");

    /**
     * ユーザー {username} は既に存在しています。
     */
    public static final I18nMessage USER_ALREADY_EXISTS = I18nMessage.of(PREFIX, "user_already_exists", List.of("username"));

    /**
     * ユーザーが既に無効化されています。<br />
     * UserID: {userId}
     */
    public static final I18nMessage USER_ALREADY_DISABLED = I18nMessage.of(PREFIX, "user_already_disabled", List.of("userId"));
}
