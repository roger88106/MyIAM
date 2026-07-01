package com.myiam.module.user.application.model.query;

import lombok.Builder;

import java.util.UUID;

/**
 * ユーザークレームクエリ
 *
 * @param userId
 */
@Builder
public record UserClaimsQuery(UUID userId) {
}
