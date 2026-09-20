package com.myiam.module.auth.server.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

/**
 * 認可情報サービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    /**
     * 認可情報リポジトリ
     */
    private final RedisOAuth2AuthorizationRepository repository;

    /**
     * 認可情報を保存または更新する。
     *
     * @param authorization 保存対象の認可情報
     */
    @Override
    public void save(@NonNull OAuth2Authorization authorization) {
        repository.save(authorization);
    }

    /**
     * 認可情報を物理削除する。
     *
     * @param authorization 削除対象の認可情報
     */
    @Override
    public void remove(@NonNull OAuth2Authorization authorization) {
        repository.remove(authorization);
    }

    /**
     * ID キーから認可情報を検索する。
     *
     * @param id 認可情報のUUID形式主キー
     * @return 該当する認可情報、見つからない場合は null
     */
    @Override
    @Nullable
    public OAuth2Authorization findById(@NonNull String id) {
        return repository.findById(id);
    }

    /**
     * トークン値およびトークン種別から認可情報を検索する。<br />
     * ※ リフレッシュトークンが再利用された場合、所属していた認可情報を丸ごと失効させる。
     *
     * @param token     トークン文字列
     * @param tokenType トークン種別（省略可能）
     * @return 該当する認可情報、見つからない場合は null
     */
    @Override
    @Nullable
    public OAuth2Authorization findByToken(@NonNull String token, @Nullable OAuth2TokenType tokenType) {
        // トークンを取得
        OAuth2Authorization authorization = repository.findByToken(token, tokenType);

        // リフレッシュトークン かつ 現行のトークン取得できない 場合
        if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType) && authorization == null) {
            // リフレッシュトークンの再利用検知処理実施
            detectionRefreshTokenReuse(token);
        }

        return authorization;
    }

    // ============================== プライベート ==============================

    /**
     * リフレッシュトークンの再利用検知処理
     *
     * @param token リフレッシュトークン
     */
    private void detectionRefreshTokenReuse(String token) {
        // リフレッシュトークンで所属の認可IDを検索する
        String authorizationId = repository.findAuthorizationIdByRotatedRefreshToken(token);

        // 結果がない場合は問題なし
        if (authorizationId == null) {
            return;
        }

        // ToDo: 正当な再送（ネットワーク再試行で旧トークンが二度届くケース）への猶予は未実装。
        //       必要なら Auth0 の reuse interval のように、直近数秒以内の再送は同じ新トークンを返す実装を検討する。

        // 再利用検知：所属していた認可情報を丸ごと失効させる
        OAuth2Authorization compromised = repository.findById(authorizationId);
        if (compromised != null) {
            repository.remove(compromised);
        }
        log.warn("refresh token reuse detected: authorizationId={}, principal={}",
                authorizationId, compromised != null ? compromised.getPrincipalName() : "unknown");
    }
}
