package com.myiam.module.auth.login.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 認証失敗ハンドラー。
 */
@Slf4j
@Component
class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    /**
     * ログインパス
     */
    @Value("${app.security.login-path}")
    private String loginPath;

    /**
     * 認証失敗時の処理。
     *
     * @param request   リクエスト
     * @param response  レスポンス
     * @param exception 認証例外
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        // ログ出力
        log.warn("login failed: user={}, type={}", request.getParameter("username"), exception.getClass().getSimpleName());

        // ToDo: 認証失敗カウンター実装（lockout はここに入る）

        // エラーキーを session に載せて（後続の GET /login で読み出す）ログイン画面へリダイレクト
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
        response.sendRedirect(request.getContextPath() + loginPath);

    }
}
