package com.myiam.module.user.domain.user;

import com.myiam.common.error.ErrorCode;
import com.myiam.common.error.SystemException;
import org.springframework.stereotype.Component;

/**
 * ユーザーファクトリー
 */
@Component
public class UserFactory {

    /**
     * スナップショットからユーザーを復元する
     *
     * @param snapshot ユーザースナップショット
     * @return ユーザー
     */
    public User restore(User.Snapshot snapshot) {
        try {
            return new User(snapshot);
        } catch (Exception e) {
            // 復元失敗の場合、復元エラーに変換してスローする
            String debugMessage = """
                    user restore error.
                    userSnapshot: %s
                    """.formatted(snapshot);
            throw SystemException.of(debugMessage, e, ErrorCode.Common.RESTORE_ERROR);
        }
    }
}
