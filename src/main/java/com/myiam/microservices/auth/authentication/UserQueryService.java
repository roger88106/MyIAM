package com.myiam.microservices.auth.authentication;

import lombok.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ユーザークエリサービス。
 */
@Service
class UserQueryService {

    /**
     * ユーザー名によるユーザー情報のロード。
     *
     * @param username ログイン対象のユーザー名
     * @return 認証認可用のユーザー情報
     * @throws UsernameNotFoundException ユーザーが見つからない場合にスローされる
     */
    UserDto.AuthUserView findUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        // TODO 別のマイクロサービスを呼び出す
        return new UserDto.AuthUserView(
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
