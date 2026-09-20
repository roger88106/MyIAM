package com.myiam.common.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 共通メッセージ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMessage {

    /**
     * 見つかりませんでした。
     */
    public static final I18nMessage NOT_FOUND = I18nMessage.of(Code.NOT_FOUND);

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

    /**
     * この操作を行う権限がありません。
     */
    public static final I18nMessage FORBIDDEN = I18nMessage.of(Code.FORBIDDEN);

    /**
     * 入力内容に誤りがあります。
     */
    public static final I18nMessage INVALID_REQUEST = I18nMessage.of(Code.INVALID_REQUEST);

    // ============================== コード定義 ==============================

    /**
     * メッセージコード
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Code{
        /**
         * 見つかりませんでした。
         */
        public static final String NOT_FOUND = "myiam.common.not_found";

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

        /**
         * この操作を行う権限がありません。
         */
        public static final String FORBIDDEN = "myiam.common.forbidden";

        /**
         * 入力内容に誤りがあります。
         */
        public static final String INVALID_REQUEST = "myiam.common.invalid_request";
    }
}
