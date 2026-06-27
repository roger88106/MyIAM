package com.myiam.module.auth.server.token;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

/**
 * 認可情報サービス
 */
@Service
@RequiredArgsConstructor
class SecurityOAuth2AuthorizationService implements OAuth2AuthorizationService {

    /** 認可情報リポジトリ */
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
     * トークン値およびトークン種別から認可情報を検索する。
     *
     * @param token トークン文字列
     * @param tokenType トークン種別（省略可能）
     * @return 該当する認可情報、見つからない場合は null
     */
    @Override
    @Nullable
    public OAuth2Authorization findByToken(@NonNull String token, @Nullable OAuth2TokenType tokenType) {
        return repository.findByToken(token, tokenType);
    }
}
