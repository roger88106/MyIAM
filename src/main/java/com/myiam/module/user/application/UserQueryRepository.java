package com.myiam.module.user.application;

import com.myiam.module.user.application.model.query.UserClaimsQuery;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.result.UserClaims;
import com.myiam.module.user.application.model.result.UserCredential;

import java.util.Optional;

/**
 * ユーザー検索リポジトリ
 */
public interface UserQueryRepository {

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findUserCredential(UserCredentialQuery query);

    /**
     * ユーザークレーム取得
     *
     * @param query ユーザークレームクエリ
     * @return ユーザークレーム
     */
    Optional<UserClaims> findUserClaims(UserClaimsQuery query);
}
