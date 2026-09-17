package com.myiam.module.auth.login.internal;

import com.myiam.module.auth.login.UserPrincipal;
import com.myiam.module.auth.userdir.UserDirectory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 認証成功ハンドラー。<br />
 * ログインを記録した後、認証前にリクエストされた URL へリダイレクトする。
 */
@Slf4j
@Component
class AuthenticationSuccessHandlerImpl extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * ユーザーディレクトリ
     */
    private final UserDirectory userDirectory;

    /**
     * {@link AuthenticationSuccessHandlerImpl} のコンストラクタ
     *
     * @param userDirectory ユーザーディレクトリ
     */
    AuthenticationSuccessHandlerImpl(UserDirectory userDirectory) {
        this.userDirectory = userDirectory;
    }

    /**
     * 認証成功時の処理。
     *
     * @param request        リクエスト
     * @param response       レスポンス
     * @param authentication 認証情報
     */
    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // ログイン記録
        if (authentication.getPrincipal() instanceof UserPrincipal(UUID userId)) {
            userDirectory.recordLogin(userId);
        }

        // ログ出力
        log.info("login succeeded: user={}", authentication.getName());

        // 認証前にリクエストされた URL へリダイレクト
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
