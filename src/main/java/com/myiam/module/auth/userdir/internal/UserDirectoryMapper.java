package com.myiam.module.auth.userdir.internal;

import com.myiam.module.auth.userdir.UserClaims;
import com.myiam.module.auth.userdir.UserCredential;
import com.myiam.module.user.api.auth.AuthUserApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ユーザーディレクトリマッパー
 */
@Mapper(componentModel = "spring")
interface UserDirectoryMapper {

    /**
     * {@link AuthUserApiResponse.Credential} を {@link UserCredential} に変換する
     *
     * @param userCredential 変換する {@code Credential}
     * @return {@code UserCredential}
     */
    UserCredential toUserCredential(AuthUserApiResponse.Credential userCredential);

    /**
     * {@link AuthUserApiResponse.UserClaims} を {@link UserClaims} に変換する
     *
     * @param userClaims 変換する {@code UserClaims}
     * @return {@code UserClaims}
     */
    @Mapping(target = "username", source = "email") // ToDo: ユーザー名分ける際に修正要
    UserClaims toUserClaims(AuthUserApiResponse.UserClaims userClaims);
}
