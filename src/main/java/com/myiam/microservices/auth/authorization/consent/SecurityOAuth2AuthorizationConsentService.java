package com.myiam.microservices.auth.authorization.consent;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Service;

/**
 * 認可同意サービス。
 */
@Service
@RequiredArgsConstructor
class SecurityOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

    /** 認可同意情報リポジトリ */
    private final OAuth2AuthorizationConsentRepository repository;

    /**
     * 認可同意情報を保存または更新する。
     *
     * @param authorizationConsent 保存対象の認可同意情報
     */
    @Override
    public void save(@NonNull OAuth2AuthorizationConsent authorizationConsent) {
        repository.save(authorizationConsent);
    }

    /**
     * 認可同意情報を物理削除する。
     *
     * @param authorizationConsent 削除対象の認可同意情報
     */
    @Override
    public void remove(@NonNull OAuth2AuthorizationConsent authorizationConsent) {
        repository.remove(authorizationConsent);
    }

    /**
     * クライアントID と 主体名 による認可同意情報を検索する。
     *
     * @param registeredClientId 登録クライアントID
     * @param principalName 主体名
     * @return 該当する認可同意情報、見つからない場合は null
     */
    @Override
    @Nullable
    public OAuth2AuthorizationConsent findById(@NonNull String registeredClientId, @NonNull String principalName) {
        return repository.findById(registeredClientId, principalName);
    }
}
