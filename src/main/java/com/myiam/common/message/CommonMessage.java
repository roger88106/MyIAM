package com.myiam.common.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 共通メッセージ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMessage {

    /**
     * フォーマットが正しくない
     */
    public static final I18nMessage INVALID_FORMAT = I18nMessage.of(Code.INVALID_FORMAT);

    /**
     * 他者に変更されました、もう一度試してください。
     */
    public static final I18nMessage CONCURRENT_MODIFICATION = I18nMessage.of(Code.CONCURRENT_MODIFICATION);

    /**
     * 予期しないエラーが発生しました。
     */
    public static final I18nMessage SYSTEM_ERROR = I18nMessage.of(Code.SYSTEM_ERROR);

    // ============================== コード定義 ==============================

    /**
     * メッセージコード
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Code{
        /**
         * フォーマットが正しくない
         */
        public static final String INVALID_FORMAT = "myiam.common.invalid_format";

        /**
         * 他者に変更されました、もう一度試してください。
         */
        public static final String CONCURRENT_MODIFICATION = "myiam.common.concurrent_modification";

        /**
         * 予期しないエラーが発生しました。
         */
        public static final String SYSTEM_ERROR = "myiam.common.system_error";
    }
}
