package com.myiam.config.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * カスタムエラーコントローラー。<br />
 * アプリケーション内で発生したエラー（HTTPエラーやOAuth2関連の例外）をハンドリングし、エラー画面を表示する。
 */
@Controller
class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {
    /**
     * エラーハンドリング処理。<br />
     * HTTPステータスコードやエラーメッセージを抽出し、エラー画面に渡すモデルを構築する。
     *
     * @param request HttpServletRequest
     * @param model Model
     * @return エラー表示用テンプレート名
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // HTTP ステータスコードを取得
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorCode = status != null ? status.toString() : "500";

        // エラーメッセージを取得
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String description = (message != null && StringUtils.hasText(message.toString()))
                ? message.toString()
                : "認証処理中にエラーが発生しました。";

        // エラー情報をモデルに追加
        model.addAttribute("error", new ErrorInfo(errorCode, description));

        // クライアントIDをモデルに追加
        model.addAttribute("clientId", request.getParameter("client_id"));

        // エラー画面のテンプレート（errorPage.html）を返す
        return "errorPage";
    }

    /**
     * エラー情報。
     *
     * @param errorCode エラーコード
     * @param description エラー説明
     */
    record ErrorInfo(String errorCode, String description) {
    }
}
