package com.myiam.module.auth.permdir;

import java.util.Set;

/**
 * サブジェクト権限情報
 *
 * @param subject     サブジェクト
 * @param roles       ロール
 * @param permissions 権限
 */
public record SubjectPermissions(String subject, Set<String> roles, Set<String> permissions) {
}
