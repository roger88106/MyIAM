package com.myiam.common.utility.error;

import com.myiam.common.error.ErrorCode;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class ErrorUtils {

    /**
     * メッセージパラメータマップを構築する
     *
     * @param errorCode エラーコード
     * @param paramValues パラメータ値
     * @return メッセージパラメータマップ
     */
    public @NonNull Map<String, String> buildMessageParams(ErrorCode errorCode, Object[] paramValues) {
        // パラメータキー取得
        List<String> paramKeys = Optional.ofNullable(errorCode.paramKeys())
                .orElse(List.of());
        // パラメータ値取得
        List<String> paramValuesList = Optional.ofNullable(paramValues)
                .map(values -> Arrays.stream(values).map(ObjectUtils::nullSafeToString).toList())
                .orElse(List.of());
        // パラメータの数が不正の場合、変換エラーをスロー
        if (paramKeys.size() != paramValuesList.size()) {
            throw new IllegalArgumentException("パラメータの数が不正です。");
        }
        // パラメータマップに変換
        return IntStream.range(0, paramValuesList.size())
                .boxed()
                .collect(Collectors.toMap(
                        paramKeys::get,
                        paramValuesList::get
                ));
    }
}
