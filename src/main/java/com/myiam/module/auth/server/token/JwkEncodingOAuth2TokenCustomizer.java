package com.myiam.module.auth.server.token;

import com.myiam.module.auth.permdir.PermissionDirectory;
import com.myiam.module.auth.userdir.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT トークンのカスタマイザー。<br />
 * トークンの発行時に、適切な識別子を設定する。
 */
@Component
@RequiredArgsConstructor
class JwkEncodingOAuth2TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    /**
     * ユーザーディレクトリ。
     */
    private final UserDirectory userDirectory;
    /**
     * 権限ディレクトリ。
     */
    private final PermissionDirectory permissionDirectory;

    /**
     * トークンカスタマイズ
     *
     * @param context JwtEncodingContext
     */
    @Override
    public void customize(JwtEncodingContext context) {

        // トークンタイプ取得
        var tokenType = context.getTokenType();

        // トークンの種類別で処理実施する
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            // アクセストークン設定
            setAccessToken(context);

        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            // IDトークン設定
            setIdTokenClaims(context);
        }
    }

    /**
     * アクセストークン設定
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setAccessToken(JwtEncodingContext context) {
        // 認可許可タイプ取得
        var authorizationGrantType = context.getAuthorizationGrantType();

        // クライアント認証（バックエンド間通信）の場合
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authorizationGrantType)) {
            // クライアント認証トークン発行
            this.setClientAccessToken(context);
        } else {
            // 上記以外の場合、ユーザー認証トークン発行
            this.setUserAccessToken(context);
        }
    }

    /**
     * IDトークン設定
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setIdTokenClaims(JwtEncodingContext context) {
        Authentication principal = context.getPrincipal();
        if (principal == null) return;

        // ユーザーIDを取得
        String userId = principal.getName();

        // JWT の "sub" (Subject) 設定
        context.getClaims().subject(userId);

        // ユーザークレーム取得
        userDirectory.findClaims(UUID.fromString(userId))
                // ユーザークレーム取得成功する場合、JWT クレーム設定
                .ifPresent(claims -> {
                    context.getClaims().claim(StandardClaimNames.PREFERRED_USERNAME, claims.username());
                    context.getClaims().claim(StandardClaimNames.EMAIL, claims.email());
                    context.getClaims().claim(StandardClaimNames.FAMILY_NAME, claims.familyName());
                    context.getClaims().claim(StandardClaimNames.GIVEN_NAME, claims.givenName());
                    context.getClaims().claim(StandardClaimNames.NAME, claims.familyName() + " " + claims.givenName());
                });

        // ロール取得
        permissionDirectory.findPermissions(userId).ifPresentOrElse(
                permissions -> {
                    // ロール取得成功する場合、JWT クレーム設定
                    context.getClaims().claim(CustomJwtClaimNames.ROLES, permissions.roles());
                },
                () -> context.getClaims().claim(CustomJwtClaimNames.ROLES, Set.of()));
    }

    /**
     * ユーザー認証のアクセストークン生成処理
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setUserAccessToken(JwtEncodingContext context) {

        Authentication principal = context.getPrincipal();
        if (principal == null) return;

        // ユーザーIDを取得
        String userId = principal.getName();

        // JWT の "sub" (Subject) 設定
        context.getClaims().subject(userId);

        // ユーザークレーム取得
        userDirectory.findClaims(UUID.fromString(userId))
                // ユーザークレーム取得成功する場合、JWT クレーム設定
                .ifPresent(claims -> {
                    context.getClaims().claim(StandardClaimNames.PREFERRED_USERNAME, claims.username());
                });

        // 権限をトークンに設定する
        setPermissionsClaim(context, userId);
    }

    /**
     * クライアント認証のアクセストークン生成処理
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setClientAccessToken(JwtEncodingContext context) {
        // sub にクライアント ID を設定
        context.getClaims().subject(context.getRegisteredClient().getClientId());

        // クライアント名称を設定
        context.getClaims().claim(CustomJwtClaimNames.CLIENT_NAME, context.getRegisteredClient().getClientName());

        // 権限をトークンに設定する
        setPermissionsClaim(context, context.getRegisteredClient().getClientId());
    }

    /**
     * 権限のクレームを設定する
     *
     * @param context JWT エンコーディングコンテキスト
     * @param subject OAuthの識別子
     */
    private void setPermissionsClaim(JwtEncodingContext context, String subject){
        // スコープ取得
        Set<String> scopes = context.getAuthorizedScopes();

        // 権限をトークンに設定する
        permissionDirectory.findPermissions(subject).ifPresentOrElse(
                // 値がある場合
                permissions -> {
                    // 権限をスコープの積集合を取得
                    Set<String> scopedPermissions = permissions.permissions()
                            .stream()
                            .filter(scopes::contains)
                            .collect(Collectors.toSet());

                    // 権限を設定する
                    context.getClaims().claim(CustomJwtClaimNames.PERMISSIONS, scopedPermissions);
                },
                // 値がない場合、空のセットを設定する
                () -> context.getClaims().claim(CustomJwtClaimNames.PERMISSIONS, Set.of()));
    }
}
