package com.myiam.module.permission.api;

import java.util.Set;

/**
 * サブジェクト権限レスポンス
 *
 * @param subject     サブジェクト
 * @param roles       ロール
 * @param permissions 権限
 */
public record SubjectPermissionResponse(String subject, Set<String> roles, Set<String> permissions) {
}
