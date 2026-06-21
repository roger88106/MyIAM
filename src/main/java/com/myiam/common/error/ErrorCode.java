package com.myiam.common.error;

import lombok.Getter;

import java.util.List;

/**
 * エラーコード
 *
 * @param code      コード
 * @param paramKeys エラーメッセージのパラメータキー
 * @param errorType エラータイプ
 */
public record ErrorCode(String code, List<String> paramKeys, ErrorType errorType) {

    /**
     * エラーコード列挙型用のインターフェース
     */
    public interface Enum {
        ErrorCode getErrorCode();
    }

    /**
     * 共通のエラーコード
     */
    @Getter
    public enum Common implements Enum {

        /**
         * 更新処理排他エラー
         */
        CONCURRENT_MODIFICATION (List.of(), ErrorType.CONFLICT_ERROR),
        /**
         * リポジトリ復元エラー
         */
        RESTORE_ERROR(List.of(), ErrorType.RESTORE_ERROR),
        ;

        /**
         * エラーコード
         */
        private final ErrorCode errorCode;

        /**
         * コンストラクタ
         */
        Common(List<String> paramKeys, ErrorType errorType) {
            this.errorCode = new ErrorCode(this.name(), paramKeys, errorType);
        }
    }
}
