package com.myiam.module.user.infrastructure;

import com.myiam.module.user.application.UserQueryRepository;
import com.myiam.module.user.application.model.query.UserClaimsQuery;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.result.UserClaims;
import com.myiam.module.user.application.model.result.UserCredential;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.myiam.jooq.user.Tables.USER_PROFILES;
import static com.myiam.jooq.user.tables.Users.USERS;

/**
 * ユーザー検索リポジトリ
 */
@Repository
@RequiredArgsConstructor
class JooqUserQueryRepository implements UserQueryRepository {

    /**
     * jOOQ DSL Context
     */
    private final DSLContext dsl;

    /**
     * ユーザーリポジトリマッパー
     */
    private final UserRepositoryMapper userRepositoryMapper;

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    @Override
    public Optional<UserCredential> findUserCredential(UserCredentialQuery query) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(query.email()))
                .fetchOptional()
                .map(userRepositoryMapper::toUserCredential);
    }

    /**
     * ユーザークレーム取得
     *
     * @param query ユーザークレームクエリ
     * @return ユーザークレーム
     */
    @Override
    public Optional<UserClaims> findUserClaims(UserClaimsQuery query) {
        return dsl.select()
                .from(USERS)
                .join(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.ID))
                .where(USERS.ID.eq(query.userId()))
                .fetchOptional()
                .map(userRepositoryMapper::toUserClaims);
    }
}
