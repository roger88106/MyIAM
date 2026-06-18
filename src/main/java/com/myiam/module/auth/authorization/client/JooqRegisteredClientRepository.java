package com.myiam.module.auth.authorization.client;

import com.myiam.jooq.auth.tables.records.Oauth2RegisteredClientRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static com.myiam.jooq.auth.tables.Oauth2RegisteredClient.OAUTH2_REGISTERED_CLIENT;

/**
 * 登録クライアント情報リポジトリ。
 */
@RequiredArgsConstructor
class JooqRegisteredClientRepository implements RegisteredClientRepository {

    /** DSL コンテキスト */
    private final DSLContext dsl;

    /** セキュリティモジュール対応のJSONマッパー */
    private final JsonMapper jsonMapper = JsonMapper.builder()
            .addModules(SecurityJacksonModules.getModules(RegisteredClientRepository.class.getClassLoader()))
            .build();

    /**
     * ID（PK）からクライアント情報を取得する。
     *
     * @param id クライアントID（文字列のUUID）
     * @return 該当するクライアント情報、見つからない場合は null
     */
    @Override
    public RegisteredClient findById(@NonNull String id) {
        return dsl.selectFrom(OAUTH2_REGISTERED_CLIENT)
                .where(OAUTH2_REGISTERED_CLIENT.ID.eq(UUID.fromString(id)))
                .fetchOne(this::parseToRegisteredClient);
    }

    /**
     * クライアントID からクライアント情報を取得する。
     *
     * @param clientId クライアント ID
     * @return 該当するクライアント情報、見つからない場合は null
     */
    @Override
    public RegisteredClient findByClientId(@NonNull String clientId) {
        return dsl.selectFrom(OAUTH2_REGISTERED_CLIENT)
                .where(OAUTH2_REGISTERED_CLIENT.CLIENT_ID.eq(clientId))
                .fetchOne(this::parseToRegisteredClient);
    }

    /**
     * クライアント情報を保存または更新する。
     *
     * @param registeredClient 登録するクライアント情報
     */
    @Override
    @Transactional
    public void save(@NonNull RegisteredClient registeredClient) {
        UUID id = UUID.fromString(registeredClient.getId());

        // 認証方式、認可グラントタイプ、各URI、スコープ一覧を文字列配列に変換
        String[] clientAuthenticationMethods = registeredClient.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue)
                .toArray(String[]::new);

        String[] authorizationGrantTypes = registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .toArray(String[]::new);

        String[] redirectUris = registeredClient.getRedirectUris().toArray(String[]::new);
        String[] postLogoutRedirectUris = registeredClient.getPostLogoutRedirectUris().toArray(String[]::new);
        String[] scopes = registeredClient.getScopes().toArray(String[]::new);

        // 既存データの存在確認および初期化
        var record = dsl.selectFrom(OAUTH2_REGISTERED_CLIENT)
                .where(OAUTH2_REGISTERED_CLIENT.ID.eq(id))
                .fetchOne();

        // 既存データ存在しない場合、新規データ作成し、登録項目を設定
        if (record == null) {
            record = dsl.newRecord(OAUTH2_REGISTERED_CLIENT);
            record.setId(id);
            record.setCreatedAt(Instant.now());
            record.setCreatedBy("system");
        } else {
            // 既存データ存在する場合、更新項目設定
            record.setUpdatedAt(Instant.now());
            record.setUpdatedBy("system");
        }

        // レコードへの各プロパティ設定
        record.setClientId(registeredClient.getClientId());
        record.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
        record.setClientSecret(registeredClient.getClientSecret());
        record.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        record.setClientName(registeredClient.getClientName());
        record.setClientAuthenticationMethods(clientAuthenticationMethods);
        record.setAuthorizationGrantTypes(authorizationGrantTypes);
        record.setRedirectUris(redirectUris);
        record.setPostLogoutRedirectUris(postLogoutRedirectUris);
        record.setScopes(scopes);
        record.setClientSettings(convertMapToJson(registeredClient.getClientSettings().getSettings()));
        record.setTokenSettings(convertMapToJson(registeredClient.getTokenSettings().getSettings()));

        // レコードの永続化を実行
        record.store();
    }

    /**
     * データベースレコードから RegisteredClient に変換する。
     *
     * @param record データベースから取得したレコード
     * @return RegisteredClient オブジェクト
     */
    private RegisteredClient parseToRegisteredClient(Oauth2RegisteredClientRecord record) {
        return RegisteredClient
                .withId(record.getId().toString())
                .clientId(record.getClientId())
                .clientIdIssuedAt(record.getClientIdIssuedAt())
                .clientSecret(record.getClientSecret())
                .clientSecretExpiresAt(record.getClientSecretExpiresAt())
                .clientName(record.getClientName())
                .clientAuthenticationMethods(methods -> {
                    if (record.getClientAuthenticationMethods() != null) {
                        Arrays.stream(record.getClientAuthenticationMethods())
                                .map(ClientAuthenticationMethod::new)
                                .forEach(methods::add);
                    }
                })
                .authorizationGrantTypes(types -> {
                    if (record.getAuthorizationGrantTypes() != null) {
                        Arrays.stream(record.getAuthorizationGrantTypes())
                                .map(AuthorizationGrantType::new)
                                .forEach(types::add);
                    }
                })
                .redirectUris(uris -> {
                    if (record.getRedirectUris() != null) {
                        uris.addAll(Arrays.asList(record.getRedirectUris()));
                    }
                })
                .postLogoutRedirectUris(uris -> {
                    if (record.getPostLogoutRedirectUris() != null) {
                        uris.addAll(Arrays.asList(record.getPostLogoutRedirectUris()));
                    }
                })
                .scopes(s -> {
                    if (record.getScopes() != null) {
                        s.addAll(Arrays.asList(record.getScopes()));
                    }
                })
                .clientSettings(ClientSettings.withSettings(convertJsonToMap(record.getClientSettings())).build())
                .tokenSettings(TokenSettings.withSettings(convertJsonToMap(record.getTokenSettings())).build())
                .build();
    }

    /**
     * JSON を Map 形式に変換する。
     *
     * @param json JsonNode
     * @return 変換後の Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertJsonToMap(JsonNode json) {
        if (json == null || json.isMissingNode()) return Map.of();
        return jsonMapper.convertValue(json, Map.class);
    }

    /**
     * Map を JSON 形式に変換する。
     *
     * @param jsonMap Map
     * @return JsonNode
     */
    private JsonNode convertMapToJson(Map<?, ?> jsonMap) {
        return jsonMapper.convertValue(jsonMap, JsonNode.class);
    }
}
