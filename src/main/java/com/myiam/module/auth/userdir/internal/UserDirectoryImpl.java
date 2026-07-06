package com.myiam.module.auth.userdir.internal;

import com.myiam.module.auth.userdir.UserClaims;
import com.myiam.module.auth.userdir.UserCredential;
import com.myiam.module.auth.userdir.UserDirectory;
import com.myiam.module.user.api.auth.AuthUserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.myiam.module.auth.shared.constant.CacheNameConst.USER_CLAIMS;

/**
 * ユーザーディレクトリ実装
 */
@Component
@RequiredArgsConstructor
class UserDirectoryImpl implements UserDirectory {

    /**
     * 認証認可ユーザーAPI
     */
    private final AuthUserApi authUserApi;

    /**
     * ユーザーディレクトリマッパー
     */
    private final UserDirectoryMapper mapper;

    /**
     * 認証情報を取得する
     *
     * @param loginId ログイン識別子
     * @return ユーザー認証情報
     */
    @Override
    public Optional<UserCredential> findCredential(String loginId) {
        return authUserApi.findCredential(loginId)
                .map(mapper::toUserCredential);
    }

    /**
     * ユーザークレームを取得する
     *
     * @param userId ユーザー ID
     * @return ユーザークレーム
     */
    @Override
    @Cacheable(cacheNames = USER_CLAIMS, key = "#userId", unless="#result.isEmpty()")
    public Optional<UserClaims> findClaims(UUID userId) {
        return authUserApi.findUserClaims(userId)
                .map(mapper::toUserClaims);
    }

}
