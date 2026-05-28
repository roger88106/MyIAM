package com.myiam.module.auth.authentication;

import lombok.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 認可ユーザーサービス。
 */
@Service
class AuthenticationUserService {

    /**
     * ユーザーIDによるユーザー情報のロード。
     *
     * @param username ログイン対象のユーザーID
     * @return 認証認可用のユーザー情報
     * @throws UsernameNotFoundException ユーザーが見つからない場合にスローされる
     */
    AuthenticationDto.AuthUserView findUserById(@NonNull String username) throws UsernameNotFoundException {
        // TODO 別のサービスを呼び出す
        return new AuthenticationDto.AuthUserView(
                "dummy_id",
                username,
                "{noop}test",
                List.of(),
                true,
                true,
                true,
                true
        );
    }
}
