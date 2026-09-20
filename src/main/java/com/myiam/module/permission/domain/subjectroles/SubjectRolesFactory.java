package com.myiam.module.permission.domain.subjectroles;

import com.myiam.common.error.CommonErrorCode;
import com.myiam.common.error.exception.SystemException;
import org.springframework.stereotype.Component;

/**
 * サブジェクトロールファクトリー
 */
@Component
public class SubjectRolesFactory {

    /**
     * スナップショットからサブジェクトロールを復元する
     *
     * @param snapshot サブジェクトロールスナップショット
     * @return サブジェクトロール
     */
    public SubjectRoles restore(SubjectRoles.Snapshot snapshot) {
        try {
            return new SubjectRoles(snapshot);
        } catch (Exception e) {
            // 復元失敗の場合、復元エラーに変換してスローする
            String debugMessage = """
                    subject roles restore error.
                    snapshot: %s
                    """.formatted(snapshot);
            throw SystemException.of(debugMessage, e, CommonErrorCode.RESTORE_ERROR);
        }
    }
}
