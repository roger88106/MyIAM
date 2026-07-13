package com.myiam.module.auth.login.controller;

import com.myiam.module.auth.login.LoginConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ログイン画面用コントローラー。<br />
 * ユーザー認証のためのログインページへの遷移を制御する。
 */
@Controller
class LoginController {

    /**
     * ログイン画面表示処理。
     *
     * @param request HTTP リクエスト
     * @param model   画面モデル
     * @return ログイン画面のテンプレート名
     */
    @GetMapping("${app.security.login-path}")
    public String login(HttpServletRequest request, Model model) {

        // セッション取得
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 認可エラー解析
            resolveAuthenticationError(model, session);
        }

        // ログイン画面を表示
        return "login";
    }

    // ============================== プライベート ==============================

    /**
     * 認可エラーを解析する。<br />
     * エラーが存在する場合、エラーメッセージを画面にセットする。
     *
     * @param model 画面モデル
     * @param session セッション
     */
    private static void resolveAuthenticationError(Model model, HttpSession session) {
        // セッションから認証エラーを取得
        if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof AuthenticationException e) {

            // 認証エラーをi18nキーに変換する
            String errorKey = switch (e) {
                case UsernameNotFoundException ignored -> LoginConst.INVALID_CREDENTIALS;
                case BadCredentialsException ignored -> LoginConst.INVALID_CREDENTIALS;
                case LockedException ignored -> LoginConst.ACCOUNT_LOCKED;
                case DisabledException ignored -> LoginConst.ACCOUNT_DISABLED;
                default -> LoginConst.UNKNOWN_ERROR;
            };

            // ToDo: メッセージをi18n変換、タイムリーフで解析するのもいい（再検討）

            // エラーメッセージを画面にセットする
            model.addAttribute(LoginConst.THYMELEAF_ERROR_MESSAGE, errorKey);

            // セッションから認証エラーを削除する
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }


}
