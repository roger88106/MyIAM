package com.myiam.module.permission.domain.subjectroles.port;

import com.myiam.module.permission.domain.subjectroles.SubjectRoles;
import org.jmolecules.ddd.annotation.Repository;

/**
 * サブジェクトロールリポジトリ
 */
@Repository
public interface SubjectRolesRepository {

    /**
     * サブジェクトロール取得。<br />
     * ※割当が一件もない場合もロール未割当のサブジェクトロールを返す。
     *
     * @param subject サブジェクト
     * @return サブジェクトロール ドメインモデル
     */
    SubjectRoles findBySubject(String subject);

    /**
     * サブジェクトロール保存
     *
     * @param subjectRoles サブジェクトロール ドメインモデル
     */
    void save(SubjectRoles subjectRoles);
}
