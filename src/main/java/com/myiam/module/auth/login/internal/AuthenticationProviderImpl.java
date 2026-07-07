package com.myiam.module.auth.login.internal;

import com.myiam.module.auth.login.UserPrincipal;
import com.myiam.module.auth.userdir.UserCredential;
import com.myiam.module.auth.userdir.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 認証プロバイダーの実装クラス。
 */
@Component
@RequiredArgsConstructor
class AuthenticationProviderImpl implements AuthenticationProvider {

    /**
     * ユーザーディレクトリ。
     */
    private final UserDirectory userDirectory;

    /** パスワードエンコーダー */
    private final PasswordEncoder encoder;

    /**
     * 指定された認証要求クラスをサポートするかどうかを判定する。<br />
     * ※現行はユーザーパスワードのみ支援する
     *
     * @param authentication 認証クラスの検証対象
     * @return UsernamePasswordAuthenticationToken またはそのサブクラスをサポートする場合は true
     */
    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 認証処理を実行。<br />
     * 処理内容は：ユーザー取得 → アカウント状態検証 → 認証処理実施
     *
     * @param authentication 認証情報トークン
     * @return 認証処理に成功した場合は、各種権限とファクター付与権限が付与された認証済みトークン
     * @throws AuthenticationException 認証情報が一致しないか、アカウントの状態に不備がある場合
     */
    @Override
    public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {

        // 認証用ユーザー取得
        var user = getAuthenticatedUser(authentication);

        // 認証処理実行
        FactorGrantedAuthority factor = verifyCredentials(authentication, user);

        // 認証済みトークン作成
        return getAuthenticatedToken(user, factor);
    }

    /**
     * 認証ユーザー取得
     *
     * @param authentication 認証情報
     * @return 有効な認証用ユーザー
     * @throws AuthenticationException 認証エラーが発生した場合
     */
    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) throws AuthenticationException {
        // ユーザーのログイン情報を取得する
        UserCredential userCredential = userDirectory.findCredential(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        // 取得したユーザーアカウントの有効性検証
        if (!userCredential.enabled()) {
            throw new DisabledException("アカウントが無効です。");
        } else if (userCredential.passwordLocked()) {
            throw new LockedException("パスワードがロックされています。");
        }

        // 権限リスト設定
        Collection<GrantedAuthority> authorities = Set.of(); // ToDo: 権限系は再検討要

        //  有効なユーザーを返却
        return AuthenticatedUser.of(userCredential.userId(), userCredential.password(), authorities);
    }

    /**
     * 認証処理実行
     *
     * @param authentication 認証情報
     * @param user 認証用ユーザー
     * @return ファクター付与権限
     */
    private @NonNull FactorGrantedAuthority verifyCredentials(@NonNull Authentication authentication, AuthenticatedUser user) {
        // パスワードエンコーダーを使用した暗号化パスワードの照合
        String rawPassword = Objects.requireNonNull(authentication.getCredentials()).toString();
        if (!encoder.matches(rawPassword, user.getPasswordValue())) {
            throw new BadCredentialsException("アカウントまたはパスワードが正しくありません。");
        }

        // ファクター付与権限（パスワード認証）の構築
        return FactorGrantedAuthority
                .withAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                .issuedAt(Instant.now())
                .build();
    }

    /**
     * 認証成功のユーザーに対して、認証トークンを生成する。
     *
     * @param user 認証用のユーザー情報
     * @param factor ファクター付与権限
     * @return 認証トークン
     */
    private static @NonNull UsernamePasswordAuthenticationToken getAuthenticatedToken(AuthenticatedUser user, FactorGrantedAuthority factor) {
        // 権限リストにファクター付与権限を追加
        Set<GrantedAuthority> authorities = new HashSet<>(user.authorities());
        authorities.add(factor);

        // ユーザ情報作成
        var principal = UserPrincipal.of(user.id());

        // 認証に成功したユーザー情報および付与された全権限を保持する認証トークンの生成
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
    }
}
