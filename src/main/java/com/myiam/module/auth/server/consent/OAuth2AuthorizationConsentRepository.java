package com.myiam.module.auth.server.consent;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static com.myiam.jooq.auth.tables.Oauth2AuthorizationConsent.OAUTH2_AUTHORIZATION_CONSENT;

/**
 * 認可同意情報リポジトリ。
 */
@Repository
@RequiredArgsConstructor
class OAuth2AuthorizationConsentRepository {

    /** DSLコンテキスト */
    private final DSLContext dsl;

    /**
     * 認可同意情報を保存する（登録または更新）。
     *
     * @param authorizationConsent 認可同意情報
     */
    void save(OAuth2AuthorizationConsent authorizationConsent) {
        UUID registeredClientId = UUID.fromString(authorizationConsent.getRegisteredClientId());
        String principalName = authorizationConsent.getPrincipalName();

        // 認可された権限リストを文字列配列に変換
        String[] authorities = authorizationConsent.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        // 既存の同意情報の取得または新規レコードの初期化
        var record = dsl.selectFrom(OAUTH2_AUTHORIZATION_CONSENT)
                .where(OAUTH2_AUTHORIZATION_CONSENT.REGISTERED_CLIENT_ID.eq(registeredClientId))
                .and(OAUTH2_AUTHORIZATION_CONSENT.PRINCIPAL_NAME.eq(principalName))
                .fetchOne();

        if (record == null) {
            record = dsl.newRecord(OAUTH2_AUTHORIZATION_CONSENT);
            record.setRegisteredClientId(registeredClientId);
            record.setPrincipalName(principalName);
            record.setCreatedAt(Instant.now());
            record.setCreatedBy("system");
        } else {
            record.setUpdatedAt(Instant.now());
            record.setUpdatedBy("system");
        }

        record.setAuthorities(authorities);

        // 保存（登録または更新）
        record.store();
    }

    /**
     * 認可同意情報を削除する。
     *
     * @param authorizationConsent 認可同意情報
     */
    void remove(OAuth2AuthorizationConsent authorizationConsent) {
        // 登録クライアントIDと認可者名称が一致する同意情報を削除
        dsl.deleteFrom(OAUTH2_AUTHORIZATION_CONSENT)
                .where(OAUTH2_AUTHORIZATION_CONSENT.REGISTERED_CLIENT_ID
                        .eq(UUID.fromString(authorizationConsent.getRegisteredClientId())))
                .and(OAUTH2_AUTHORIZATION_CONSENT.PRINCIPAL_NAME.eq(authorizationConsent.getPrincipalName()))
                .execute();
    }

    /**
     * クライアントID と 主体名 による認可同意情報の検索。
     *
     * @param registeredClientId 登録クライアントID
     * @param principalName 主体名
     * @return 認可同意情報
     */
    @Nullable
    OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        var record = dsl.selectFrom(OAUTH2_AUTHORIZATION_CONSENT)
                .where(OAUTH2_AUTHORIZATION_CONSENT.REGISTERED_CLIENT_ID.eq(UUID.fromString(registeredClientId)))
                .and(OAUTH2_AUTHORIZATION_CONSENT.PRINCIPAL_NAME.eq(principalName))
                .fetchOne();

        if (record == null) {
            return null;
        }

        // 認可同意情報に変換する
        var builder = OAuth2AuthorizationConsent.withId(registeredClientId, principalName);

        // 権限情報を設定
        Set.of(record.getAuthorities()).stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(builder::authority);

        return builder.build();
    }
}
