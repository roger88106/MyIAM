package com.myiam.module.user.application.model.query;

import lombok.Builder;

/**
 * ユーザー認証情報クエリ
 *
 * @param email
 */
@Builder
public record UserCredentialQuery(String email) {
}
