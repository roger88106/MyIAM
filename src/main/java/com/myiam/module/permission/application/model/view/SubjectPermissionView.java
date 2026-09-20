package com.myiam.module.permission.application.model.view;

import lombok.Builder;

import java.util.Set;

/**
 * サブジェクトの権限ビュー
 *
 * @param subject     サブジェクト
 * @param roles       ロールリスト
 * @param permissions 権限リスト
 */
@Builder
public record SubjectPermissionView(String subject, Set<String> roles, Set<String> permissions) {
}
