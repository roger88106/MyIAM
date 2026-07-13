package com.myiam.module.auth.login;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ログイン用の定数
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginConst {

    // ============================== i18nキー ==============================

    /**
     * ユーザーの資格情報が無効
     */
    public static final String INVALID_CREDENTIALS = "login.error.invalid_credentials";

    /**
     * アカウントがロックされている
     */
    public static final String ACCOUNT_LOCKED = "login.error.account_locked";

    /**
     * アカウントが無効になっている
     */
    public static final String ACCOUNT_DISABLED = "login.error.account_disabled";

    /**
     * 予期せぬエラー
     */
    public static final String UNKNOWN_ERROR = "login.error.unknown";

    // ============================== タイムリーフのモデルキー ==============================

    /**
     * エラーメッセージ
     */
    public static final String THYMELEAF_ERROR_MESSAGE = "errorMessage";
}
