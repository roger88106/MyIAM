package com.myiam.module.auth.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

/**
 * ログイン画面用コントローラー。<br />
 * ユーザー認証のためのログインページへの遷移を制御する。
 */
@Controller
public class LoginController {

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    /**
     * ログイン画面表示処理。<br />
     * Remember-Me等で既に認証済みの場合は、元のリクエスト先（例: /oauth2/authorize）にリダイレクトする。<br />
     * 未認証の場合はカスタムログインページのテンプレートを表示する。
     *
     * @param authentication 現在の認証情報
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @return ログイン画面のテンプレート名、または元のリクエスト先へのリダイレクト
     */
    @GetMapping("/login")
    public String login(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        // 既に認証済みの場合、元のリクエスト先にリダイレクト
        if (authentication != null && authentication.isAuthenticated()) {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String redirectUrl = savedRequest.getRedirectUrl();
                // 相対パスの場合
                if (redirectUrl.startsWith("/")) {
                    // 対応する相対パスにリダイレクト
                    return "redirect:" + redirectUrl;
                }
                try {
                    // 相対パス以外の場合、URLのサーバー名を比較
                    URI uri = URI.create(redirectUrl);
                    if (request.getServerName().equals(uri.getHost())) {
                        // サーバー名が一致する場合、リダイレクトする
                        return "redirect:" + redirectUrl;
                    }
                } catch (Exception ignored) {
                }
            }

            // URI解析失敗時は安全のためトップページへ
            return "redirect:/";
        }

        // 未認証の場合、ログイン画面を表示
        return "login";
    }
}
