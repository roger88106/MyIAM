package com.myiam.module.permission.domain.subjectroles;

import com.myiam.common.message.I18nMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 権限関連のメッセージ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionMessage {

    /**
     * ロール {role} が見つかりません。
     */
    public static final I18nMessage ROLE_NOT_FOUND = I18nMessage.of(Code.ROLE_NOT_FOUND, List.of("role"));

    // ============================== コード定義 ==============================

    /**
     * メッセージコード
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Code {
        /**
         * ロール {role} が見つかりません。
         */
        public static final String ROLE_NOT_FOUND = "myiam.permission.role_not_found";
    }
}
