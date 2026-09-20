package com.myiam.module.permission.application;

import com.myiam.module.permission.application.model.view.SubjectPermissionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 権限クエリーサービス
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionQueryService {

    /**
     * 権限クエリリポジトリ
     */
    private final PermissionQueryRepository permissionQueryRepository;

    /**
     * サブジェクトから権限を取得する
     *
     * @param subject サブジェクト
     * @return サブジェクトの権限ビュー
     */
    public SubjectPermissionView getPermissionByUserSubject(String subject) {
        return permissionQueryRepository.findPermissionByUserSubject(subject)
                // 検索結果がない場合、デフォルトの値を戻す
                .orElse(SubjectPermissionView.builder()
                        .subject(subject)
                        .roles(Set.of())
                        .permissions(Set.of())
                        .build());
    }
}
