package com.myiam.common.error;

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
     * システムエラー
     */
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode("SYSTEM_ERROR", List.of(), ErrorType.SYSTEM_ERROR);

    /**
     * エラーコード列挙型用のインターフェース
     */
    public interface Enum {
        ErrorCode getErrorCode();
    }
}
