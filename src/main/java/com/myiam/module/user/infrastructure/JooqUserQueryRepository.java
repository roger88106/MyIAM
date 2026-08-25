package com.myiam.module.user.infrastructure;

import com.myiam.module.user.application.UserQueryRepository;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.view.UserClaimsView;
import com.myiam.module.user.application.model.view.UserCredentialView;
import com.myiam.module.user.application.model.view.UserDetailView;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

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
     * IDでユーザー取得
     *
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    @Override
    public Optional<UserDetailView> findUserById(UUID userId) {
        return dsl.select()
                .from(USERS)
                .leftJoin(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.ID))
                .where(USERS.ID.eq(userId))
                .and(USERS.ENABLED.eq(true))
                .fetchOptional()
                .map(userRepositoryMapper::toUserDetailView);
    }

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    @Override
    public Optional<UserCredentialView> findUserCredential(UserCredentialQuery query) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(query.email()))
                .fetchOptional()
                .map(userRepositoryMapper::toUserCredential);
    }

    /**
     * ユーザークレーム取得
     *
     * @param userId ユーザーID
     * @return ユーザークレーム
     */
    @Override
    public Optional<UserClaimsView> findUserClaims(UUID userId) {
        return dsl.select()
                .from(USERS)
                .leftJoin(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.ID))
                .where(USERS.ID.eq(userId))
                .and(USERS.ENABLED.eq(true))
                .fetchOptional()
                .map(userRepositoryMapper::toUserClaims);
    }
}
