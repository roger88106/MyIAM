package com.myiam.microservices.auth.core.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * カスタムエラーコントローラー。<br />
 * アプリケーション内で発生したエラー（HTTPエラーやOAuth2関連の例外）をハンドリングし、エラー画面を表示する。
 */
@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {
    /**
     * エラーハンドリング処理。<br />
     * HTTPステータスコードやエラーメッセージを抽出し、エラー画面に渡すモデルを構築する。
     *
     * @param request HttpServletRequest
     * @param model   Model
     * @return エラー表示用テンプレート名
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // HTTP ステータスコード（404, 500 等）を取得
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // エラーメッセージを取得（存在する場合）
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        // クライアントIDを取得（OAuth2 エラーによる遷移時に含まれる場合がある）
        String clientId = request.getParameter("client_id");

        // テンプレートに渡すためのエラー情報マップを作成
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("errorCode", status != null ? "Status " + status : "OAuth2 Error");

        // エラー詳細メッセージの設定（メッセージが空の場合はデフォルト文言を使用）
        String description = (message != null && !message.toString().isEmpty())
                ? message.toString()
                : "認証リクエストの処理中にエラーが発生しました。";
        errorMap.put("description", description);

        model.addAttribute("error", errorMap);
        model.addAttribute("clientId", clientId);

        // エラー画面のテンプレート（errorPage.html）を返す
        return "errorPage";
    }
}
