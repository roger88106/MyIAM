package com.myiam.module.user.api.auth;

import com.myiam.module.user.application.UserQueryService;
import com.myiam.module.user.application.model.query.UserClaimsQuery;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * 認証認可モジュール向けの ユーザーAPI
 */
@Component
@RequiredArgsConstructor
public class AuthUserApi {

    /**
     * ユーザークエリサービス
     */
    private final UserQueryService userQueryService;

    /**
     * 認証認可ユーザーマッパー
     */
    private final AuthUserMapper authUserMapper;

    /**
     * ユーザーの認証情報取得
     *
     * @param email メールアドレス
     * @return 認証情報
     */
    public Optional<AuthUserApiResponse.Credential> getCredential(String email) {
        var query = UserCredentialQuery.builder()
                .email(email)
                .build();

        return userQueryService.getUserCredential(query)
                .map(authUserMapper::toCredential);
    }

    /**
     * ユーザーの認証情報取得
     *
     * @param userId ユーザー ID
     * @return 認証情報
     */
    public Optional<AuthUserApiResponse.UserClaims> getUserClaims(UUID userId) {
        var query = UserClaimsQuery.builder()
                .userId(userId)
                .build();

        return userQueryService.getUserClaims(query)
                .map(authUserMapper::toUserClaims);
    }
}
