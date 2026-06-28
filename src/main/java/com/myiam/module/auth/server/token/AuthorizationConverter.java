package com.myiam.module.auth.server.token;

import com.myiam.module.auth.login.AuthenticationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 認可情報のコンバータークラス。
 */
@Component
@RequiredArgsConstructor
class AuthorizationConverter {
    /**
     * JSON マッパー
     */
    private final JsonMapper jsonMapper = builderSecurityJsonMapper();

    private final RegisteredClientRepository registeredClientRepository;

    /**
     * 認可情報ドメイン(OAuth2Authorization) を Json にシリアライズする。
     *
     * @param authorization 認可情報ドメイン
     * @return 認可情報Json
     */
    String serOAuth2Authorization(OAuth2Authorization authorization) {
        if (authorization == null) return null;

        // 認可情報 PO を作成
        var poBuilder = AuthorizationPo.builder()
                // 識別子
                .id(authorization.getId())
                // 登録クライアント ID
                .registeredClientId(authorization.getRegisteredClientId())
                // 主体名
                .principalName(authorization.getPrincipalName())
                // 認可グラントタイプ
                .authorizationGrantType(authorization.getAuthorizationGrantType().getValue())
                // 認可済みスコープ
                .authorizedScopes(StringUtils.collectionToDelimitedString(authorization.getAuthorizedScopes(), ","))
                // 属性情報 (JSON文字列)
                .attributes(serMapToJson(authorization.getAttributes()))
                // ステート
                .state(authorization.getAttribute(OAuth2ParameterNames.STATE));

        // 認可コード情報の抽出と設定
        var authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
        setTokenValues(authorizationCode, poBuilder::authorizationCodeValue, poBuilder::authorizationCodeIssuedAt,
                poBuilder::authorizationCodeExpiresAt, poBuilder::authorizationCodeMetadata);

        // アクセストークン情報の抽出と設定
        var accessToken = authorization.getToken(OAuth2AccessToken.class);
        setTokenValues(accessToken, poBuilder::accessTokenValue, poBuilder::accessTokenIssuedAt,
                poBuilder::accessTokenExpiresAt, poBuilder::accessTokenMetadata);
        if (accessToken != null && accessToken.getToken() != null) {
            // トークンタイプ設定
            if (accessToken.getToken().getTokenType() != null) {
                poBuilder.accessTokenType(accessToken.getToken().getTokenType().getValue());
            }
            // スコープ設定
            if (!accessToken.getToken().getScopes().isEmpty()) {
                poBuilder.accessTokenScopes(StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
            }
        }

        // OIDC IDトークン情報の抽出と設定
        var oidcIdToken = authorization.getToken(OidcIdToken.class);
        setTokenValues(oidcIdToken, poBuilder::oidcIdTokenValue, poBuilder::oidcIdTokenIssuedAt, poBuilder::oidcIdTokenExpiresAt, poBuilder::oidcIdTokenMetadata);

        // リフレッシュトークン情報の抽出と設定
        var refreshToken = authorization.getToken(OAuth2RefreshToken.class);
        setTokenValues(refreshToken, poBuilder::refreshTokenValue, poBuilder::refreshTokenIssuedAt, poBuilder::refreshTokenExpiresAt, poBuilder::refreshTokenMetadata);

        // ユーザーコード情報の抽出と設定
        var userCode = authorization.getToken(OAuth2UserCode.class);
        setTokenValues(userCode, poBuilder::userCodeValue, poBuilder::userCodeIssuedAt, poBuilder::userCodeExpiresAt, poBuilder::userCodeMetadata);

        // デバイスコード情報の抽出と設定
        var deviceCode = authorization.getToken(OAuth2DeviceCode.class);
        setTokenValues(deviceCode, poBuilder::deviceCodeValue, poBuilder::deviceCodeIssuedAt, poBuilder::deviceCodeExpiresAt, poBuilder::deviceCodeMetadata);

        // json に変換する
        return jsonMapper.writeValueAsString(poBuilder.build());
    }

    /**
     * 認可情報永続化オブジェクト から 認可情報ドメイン(OAuth2Authorization) に変換する。
     *
     * @param json 認可情報永続化オブジェクト
     * @return 認可情報ドメイン
     */
    OAuth2Authorization toAuthorization(String json) {
        if (json == null || json.isEmpty()) return null;

        // json を PO に変換する
        AuthorizationPo po = jsonMapper.readValue(json, AuthorizationPo.class);
        if (po == null) return null;

        // 認可クライアント取得
        RegisteredClient registeredClient = registeredClientRepository.findById(po.registeredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '%s' was not found."
                            .formatted(po.registeredClientId())
            );
        }

        // 認可情報のビルダー設定
        var builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                // 識別子
                .id(po.id())
                // 主体名
                .principalName(po.principalName())
                // 認可グラントタイプ
                .authorizationGrantType(new AuthorizationGrantType(po.authorizationGrantType()))
                // 認可済みスコープ
                .authorizedScopes(StringUtils.commaDelimitedListToSet(po.authorizedScopes()))
                // 属性
                .attributes(attributes -> attributes.putAll(desJsonToMap(po.attributes())));

        // STATE設定
        if (StringUtils.hasText(po.state())) {
            builder.attribute(OAuth2ParameterNames.STATE, po.state());
        }

        // 認可コードの復元
        if (StringUtils.hasText(po.authorizationCodeValue())) {
            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(po.authorizationCodeValue(), po.authorizationCodeIssuedAt(), po.authorizationCodeExpiresAt());
            builder.token(authorizationCode, metadata -> metadata.putAll(desJsonToMap(po.authorizationCodeMetadata())));
        }

        // アクセストークンの復元
        if (StringUtils.hasText(po.accessTokenValue())) {
            OAuth2AccessToken.TokenType tokenType = OAuth2AccessToken.TokenType.BEARER;

            if (StringUtils.hasText(po.accessTokenType())
                    && !OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(po.accessTokenType())) {
                tokenType = new OAuth2AccessToken.TokenType(po.accessTokenType());
            }
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    tokenType,
                    po.accessTokenValue(),
                    po.accessTokenIssuedAt(),
                    po.accessTokenExpiresAt(),
                    StringUtils.commaDelimitedListToSet(po.accessTokenScopes()));
            builder.token(accessToken, metadata -> metadata.putAll(desJsonToMap(po.accessTokenMetadata())));
        }

        // OIDC IDトークンの復元
        if (StringUtils.hasText(po.oidcIdTokenValue())) {
            Map<String, Object> oidcTokenMetadata = desJsonToMap(po.oidcIdTokenMetadata());
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = (Map<String, Object>) oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME);
            OidcIdToken idToken = new OidcIdToken(po.oidcIdTokenValue(), po.oidcIdTokenIssuedAt(), po.oidcIdTokenExpiresAt(), claims);
            builder.token(idToken, metadata -> metadata.putAll(oidcTokenMetadata));
        }

        // リフレッシュトークンの復元
        if (StringUtils.hasText(po.refreshTokenValue())) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(po.refreshTokenValue(), po.refreshTokenIssuedAt(), po.refreshTokenExpiresAt());
            builder.token(refreshToken, metadata -> metadata.putAll(desJsonToMap(po.refreshTokenMetadata())));
        }

        // ユーザーコードの復元
        if (StringUtils.hasText(po.userCodeValue())) {
            OAuth2UserCode userCode = new OAuth2UserCode(po.userCodeValue(), po.userCodeIssuedAt(), po.userCodeExpiresAt());
            builder.token(userCode, metadata -> metadata.putAll(desJsonToMap(po.userCodeMetadata())));
        }

        // デバイスコードの復元
        if (StringUtils.hasText(po.deviceCodeValue())) {
            OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(po.deviceCodeValue(), po.deviceCodeIssuedAt(), po.deviceCodeExpiresAt());
            builder.token(deviceCode, metadata -> metadata.putAll(desJsonToMap(po.deviceCodeMetadata())));
        }

        return builder.build();
    }

    /**
     * トークン情報をエンティティの各フィールドに振り分ける補助メソッド。
     *
     * @param token トークン情報
     * @param tokenValueConsumer トークン値の設定関数
     * @param issuedAtConsumer 発行日時の設定関数
     * @param expiresAtConsumer 有効期限の設定関数
     * @param metadataConsumer メタデータの設定関数
     */
    private void setTokenValues(OAuth2Authorization.Token<?> token, Consumer<String> tokenValueConsumer, Consumer<Instant> issuedAtConsumer, Consumer<Instant> expiresAtConsumer, Consumer<String> metadataConsumer) {
        if (token == null) return;

        OAuth2Token oAuth2Token = token.getToken();
        tokenValueConsumer.accept(oAuth2Token.getTokenValue());
        issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
        expiresAtConsumer.accept(oAuth2Token.getExpiresAt());
        metadataConsumer.accept(serMapToJson(token.getMetadata()));
    }

    /**
     * Map を JSON にシリアライズする。
     *
     * @param data 変換対象の Map
     * @return JSON 文字列
     * @throws JacksonException JSON デシリアライズに失敗した場合
     */
    private String serMapToJson(Map<String, Object> data) throws JacksonException {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        return jsonMapper.writeValueAsString(data);
    }

    /**
     * JSON を Map にデシリアライズする
     *
     * @param data 解析対象の JSON 文字列
     * @return 解析された Map（空の場合は空の Map）
     * @throws JacksonException JSON デシリアライズに失敗した場合
     */
    private Map<String, Object> desJsonToMap(String data) throws JacksonException {
        if (!StringUtils.hasText(data)) {
            return Collections.emptyMap();
        }
        return jsonMapper.readValue(data, new TypeReference<>() {
        });
    }

    /**
     * SpringSecurity 用の JsonMapper を構築
     *
     * @return JsonMapper マッパー
     */
    private JsonMapper builderSecurityJsonMapper() {
        // ポリモーフィック型検証ビルダーを初期化
        var bpt = BasicPolymorphicTypeValidator
                .builder();

        // ホワイトリストに登録されたクラスをサブタイプとして許可
        bpt.allowIfSubType(AuthenticationDto.UserView.class);

        // セキュリティモジュールを含む JsonMapper を構築
        return JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(), bpt))
                .build();
    }

}
