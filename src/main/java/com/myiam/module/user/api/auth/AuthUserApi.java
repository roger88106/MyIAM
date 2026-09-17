package com.myiam.module.user.api.auth;

import com.myiam.module.user.application.UserCommandService;
import com.myiam.module.user.application.UserQueryService;
import com.myiam.module.user.application.model.command.LockPasswordCommand;
import com.myiam.module.user.application.model.command.RecordLoginCommand;
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
     * ユーザーコマンドサービス
     */
    private final UserCommandService userCommandService;

    /**
     * 認証認可ユーザーマッパー
     */
    private final AuthUserMapper authUserMapper;

    // ============================== クエリ ==============================

    /**
     * ユーザーの認証情報取得
     *
     * @param email メールアドレス
     * @return 認証情報
     */
    public Optional<AuthUserApiResponse.Credential> findCredential(String email) {
        var query = UserCredentialQuery.builder()
                .email(email)
                .build();

        return userQueryService.findUserCredential(query)
                .map(authUserMapper::toCredential);
    }

    /**
     * ユーザークレーム取得
     *
     * @param userId ユーザー ID
     * @return ユーザークレーム
     */
    public Optional<AuthUserApiResponse.UserClaims> findUserClaims(UUID userId) {
        return userQueryService.findUserClaims(userId)
                .map(authUserMapper::toUserClaims);
    }

    // ============================== コマンド ==============================

    /**
     * ログイン記録
     *
     * @param userId ユーザー ID
     */
    public void recordLogin(UUID userId) {
        userCommandService.recordLogin(new RecordLoginCommand(userId));
    }

    /**
     * パスワードロック
     *
     * @param userId ユーザー ID
     */
    public void lockPassword(UUID userId) {
        userCommandService.lockPassword(new LockPasswordCommand(userId));
    }
}
