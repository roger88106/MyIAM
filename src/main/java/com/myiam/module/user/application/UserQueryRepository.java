package com.myiam.module.user.application;

import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.view.UserClaimsView;
import com.myiam.module.user.application.model.view.UserCredentialView;
import com.myiam.module.user.application.model.view.UserDetailView;

import java.util.Optional;
import java.util.UUID;

/**
 * ユーザー検索リポジトリ
 */
public interface UserQueryRepository {

    /**
     * IDでユーザー取得
     *
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    Optional<UserDetailView> findUserById(UUID userId);

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    Optional<UserCredentialView> findUserCredential(UserCredentialQuery query);

    /**
     * ユーザークレーム取得
     *
     * @param userId ユーザーID
     * @return ユーザークレーム
     */
    Optional<UserClaimsView> findUserClaims(UUID userId);
}
