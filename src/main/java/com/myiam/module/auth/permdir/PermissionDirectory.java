package com.myiam.module.auth.permdir;

import java.util.Optional;

/**
 * 権限ディレクトリ。<br />
 * サブジェクトの権限情報を取得するためのインターフェース。
 */
public interface PermissionDirectory {

    /**
     * サブジェクトの権限情報を取得する。<br />
     *
     * @param subject サブジェクト
     * @return サブジェクト権限情報
     */
    Optional<SubjectPermissions> findPermissions(String subject);
}
