package com.myiam.module.auth.authorization.token;

import lombok.Builder;

import java.time.Instant;

/**
 * 認可情報の永続化オブジェクト。<br />
 * オブジェクト設計は Spring 公式の DDL
 * <uri>
 * <a href="https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql">oauth2_authorization</a>
 * </uri>
 * に参照した設計。
 *
 * @param id 識別子
 * @param registeredClientId 登録クライアントID
 * @param principalName 主体名
 * @param authorizationGrantType 認可グラントタイプ
 * @param authorizedScopes 認可済みスコープ
 * @param attributes 属性情報 (JSON文字列)
 * @param state ステート
 * <br />
 * @param authorizationCodeValue 認可コード値
 * @param authorizationCodeIssuedAt 認可コード発行日時
 * @param authorizationCodeExpiresAt 認可コード有効期限
 * @param authorizationCodeMetadata 認可コードメタデータ (JSON文字列)
 * <br />
 * @param accessTokenValue アクセストークン値
 * @param accessTokenIssuedAt アクセストークン発行日時
 * @param accessTokenExpiresAt アクセストークン有効期限
 * @param accessTokenMetadata アクセストークンメタデータ (JSON文字列)
 * @param accessTokenType アクセストークンタイプ
 * @param accessTokenScopes アクセストークンスコープ
 * <br />
 * @param oidcIdTokenValue OIDC IDトークン値
 * @param oidcIdTokenIssuedAt OIDC IDトークン発行日時
 * @param oidcIdTokenExpiresAt OIDC IDトークン有効期限
 * @param oidcIdTokenMetadata OIDC IDトークンメタデータ (JSON文字列)
 * <br />
 * @param refreshTokenValue リフレッシュトークン値
 * @param refreshTokenIssuedAt リフレッシュトークン発行日時
 * @param refreshTokenExpiresAt リフレッシュトークン有効期限
 * @param refreshTokenMetadata リフレッシュトークンメタデータ (JSON文字列)
 * <br />
 * @param userCodeValue ユーザーコード値
 * @param userCodeIssuedAt ユーザーコード発行日時
 * @param userCodeExpiresAt ユーザーコード有効期限
 * @param userCodeMetadata ユーザーコードメタデータ (JSON文字列)
 * <br />
 * @param deviceCodeValue デバイスコード値
 * @param deviceCodeIssuedAt デバイスコード発行日時
 * @param deviceCodeExpiresAt デバイスコード有効期限
 * @param deviceCodeMetadata デバイスコードメタデータ (JSON文字列)
 */
@Builder
record AuthorizationPo(String id, String registeredClientId, String principalName, String authorizationGrantType,
                       String authorizedScopes, String attributes, String state, String authorizationCodeValue,
                       Instant authorizationCodeIssuedAt, Instant authorizationCodeExpiresAt,
                       String authorizationCodeMetadata, String accessTokenValue, Instant accessTokenIssuedAt,
                       Instant accessTokenExpiresAt, String accessTokenMetadata, String accessTokenType,
                       String accessTokenScopes, String oidcIdTokenValue, Instant oidcIdTokenIssuedAt,
                       Instant oidcIdTokenExpiresAt, String oidcIdTokenMetadata, String refreshTokenValue,
                       Instant refreshTokenIssuedAt, Instant refreshTokenExpiresAt, String refreshTokenMetadata,
                       String userCodeValue, Instant userCodeIssuedAt, Instant userCodeExpiresAt,
                       String userCodeMetadata, String deviceCodeValue, Instant deviceCodeIssuedAt,
                       Instant deviceCodeExpiresAt, String deviceCodeMetadata) {
}