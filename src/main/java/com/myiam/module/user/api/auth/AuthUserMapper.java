package com.myiam.module.user.api.auth;

import com.myiam.module.user.application.model.view.UserClaimsView;
import com.myiam.module.user.application.model.view.UserCredentialView;
import org.mapstruct.Mapper;

/**
 * 認証認可ユーザーマッパー
 */
@Mapper(componentModel = "spring")
interface AuthUserMapper {
    /**
     * {@link UserCredentialView} を {@link AuthUserApiResponse.Credential} に変換する
     *
     * @param userCredentialView 変換する {@code UserCredential}
     * @return {@code Credential}
     */
    AuthUserApiResponse.Credential toCredential(UserCredentialView userCredentialView);

    /**
     * {@link UserClaimsView} を {@link AuthUserApiResponse.UserClaims} に変換する
     *
     * @param userClaimsView 変換する {@code UserClaims}
     * @return {@code UserClaims}
     */
    AuthUserApiResponse.UserClaims toUserClaims(UserClaimsView userClaimsView);
}
