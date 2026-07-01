package com.myiam.module.user.api.auth;

import com.myiam.module.user.application.model.result.UserClaims;
import com.myiam.module.user.application.model.result.UserCredential;
import org.mapstruct.Mapper;

/**
 * 認証認可ユーザーマッパー
 */
@Mapper(componentModel = "spring")
interface AuthUserMapper {
    /**
     * {@link UserCredential} を {@link AuthUserApiResponse.Credential} に変換する
     *
     * @param userCredential 変換する {@code UserCredential}
     * @return {@code Credential}
     */
    AuthUserApiResponse.Credential toCredential(UserCredential userCredential);

    /**
     * {@link UserClaims} を {@link AuthUserApiResponse.UserClaims} に変換する
     *
     * @param userClaims 変換する {@code UserClaims}
     * @return {@code UserClaims}
     */
    AuthUserApiResponse.UserClaims toUserClaims(UserClaims userClaims);
}
