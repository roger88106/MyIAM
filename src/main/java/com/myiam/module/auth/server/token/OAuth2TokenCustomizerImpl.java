package com.myiam.module.auth.server.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/**
 * JWT トークンのカスタマイザー。<br />
 * トークンの発行時に、適切な識別子を設定する。
 */
@Component
class OAuth2TokenCustomizerImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {

    /**
     * トークンカスタマイズ
     *
     * @param context JwtEncodingContext
     */
    @Override
    public void customize(JwtEncodingContext context) {

        // トークンタイプ取得
        var tokenType = context.getTokenType();
        // 認可許可タイプ取得
        var authorizationGrantType = context.getAuthorizationGrantType();

        // トークンの種類がアクセストークンの場合のみ処理を行う
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {

            // クライアント認証（バックエンド間通信）の場合
            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authorizationGrantType)) {
                // クライアント認証トークン発行
                this.setClientAccessToken(context);
            } else {
                // 上記以外の場合、ユーザー認証トークン発行
                this.setUserAccessToken(context);
            }
        }
    }

    /**
     * ユーザー認証のアクセストークン生成処理
     *
     * @param context JWT エンコーディングコンテキスト
     */
    private void setUserAccessToken(JwtEncodingContext context) {

        Authentication principal = context.getPrincipal();
        if (principal == null) return;

        // JWT の "sub" (Subject) クレームにprincipalの名前を設定
        context.getClaims().subject(principal.getName());

        // ToDo: ユーザー情報取得方法修正要
//        // principalがカスタムのユーザの場合
//        if (principal.getPrincipal() instanceof AuthenticationDto.UserView user) {
//            // カスタムクレーム：ユーザー名追加
//            context.getClaims().claim("user_name", user.username());
//        }
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
        context.getClaims().claim("client_name", context.getRegisteredClient().getClientName());
    }
}
