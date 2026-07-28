package com.myiam.common.message;

import com.myiam.common.error.ErrorDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * メッセージヘルパー。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHelper {

    /**
     * メッセージソース
     */
    private final MessageSource messageSource;

    /**
     * デフォルトメッセージを取得する。
     *
     * @param code I18nコード
     * @return デフォルトメッセージ
     */
    public String getDefaultMessage(String code) {
        if (code.isBlank()) {
            return "";
        } else {
            return getMessageTemplate(code);
        }
    }

    /**
     * デフォルトメッセージを取得する。
     *
     * @param errorDetail エラー明細
     * @return デフォルトメッセージ
     */
    public String getDefaultMessage(ErrorDetail errorDetail) {
        ErrorMessage message = errorDetail.message();
        if (message.code().isBlank()) {
            return "";
        } else {
            String defaultMessage = getMessageTemplate(message.code());
            return formatMessage(defaultMessage, message.params());
        }
    }

    // ============================== プライベートメソッド ==============================

    /**
     * メッセージをフォーマットする。
     *
     * @param template メッセージテンプレート
     * @param params パラメータマップ
     * @return フォーマット済みメッセージ
     */
    private String formatMessage(String template, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return template;
        }
        // パラメータをテンプレートに置換
        for (Map.Entry<String, String> entry : params.entrySet()) {
            template = template.replace("{%s}".formatted(entry.getKey()), String.valueOf(entry.getValue())
            );
        }
        return template;
    }

    /**
     * メッセージテンプレートを取得する。
     *
     * @param code メッセージコード
     * @return メッセージテンプレート
     */
    private String getMessageTemplate(String code) {
        try {
            return messageSource.getMessage(code, null,null);
        } catch (NoSuchMessageException e) {
            log.error("メッセージ未設定。code={}", code);
            return code;
        }
    }
}
