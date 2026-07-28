package com.myiam.common.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 共通メッセージ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMessage {

    /**
     * メッセージプレフィックス
     */
    private static final String MESSAGE_PREFIX = "myiam.common";

    // ============================== メッセージ定義 ==============================

    /**
     * フォーマットが正しくない
     */
    public static final I18nMessage INVALID_FORMAT = I18nMessage.of(Code.INVALID_FORMAT_CODE);

    /**
     * 他者に変更されました、もう一度試してください。
     */
    public static final I18nMessage CONCURRENT_MODIFICATION = I18nMessage.of(MESSAGE_PREFIX, "concurrent_modification");

    /**
     * 予期しないエラーが発生しました。
     */
    public static final I18nMessage SYSTEM_ERROR = I18nMessage.of(MESSAGE_PREFIX, "system_error");

    // ============================== メッセージコード定義 ==============================

    /**
     * メッセージコード <br />
     * ※アノテーション利用する想定のメッセージコード定義
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Code {
        /**
         * メッセージコード：フォーマットが正しくない
         */
        public static final String INVALID_FORMAT_CODE = MESSAGE_PREFIX + ".invalid_format";
    }

}
