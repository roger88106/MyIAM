package com.myiam.module.user.infrastructure;

import com.myiam.jooq.user.tables.records.UsersRecord;
import com.myiam.module.user.application.model.view.UserClaimsView;
import com.myiam.module.user.application.model.view.UserCredentialView;
import com.myiam.module.user.application.model.view.UserDetailView;
import org.jooq.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static com.myiam.jooq.user.Tables.USERS;
import static com.myiam.jooq.user.Tables.USER_PROFILES;

/**
 * ユーザーリポジトリマッパー
 */
@Mapper(componentModel = "spring")
interface UserRepositoryMapper {

// ============================== MapStruct 変換 ==============================

    /**
     * {@link UsersRecord} を {@link UserCredentialView} に変換する
     *
     * @param record 変換する {@code UsersRecord}
     * @return {@code UserCredential}
     */
    @Mapping(target = "userId", source = "id")
    UserCredentialView toUserCredential(UsersRecord record);

// ============================== デフォルトメソッド変換 ==============================

    /**
     * ユーザー明細に変換する
     *
     * @param record jOOQレコード
     * @return {@link UserDetailView}
     */
    default UserDetailView toUserDetailView(Record record) {
        return UserDetailView.builder()
                .userId(record.get(USERS.ID))
                .email(record.get(USERS.EMAIL))
                .lastLoginAt(record.get(USERS.LAST_LOGIN_AT))
                .familyName(record.get(USER_PROFILES.FAMILY_NAME))
                .givenName(record.get(USER_PROFILES.GIVEN_NAME))
                .build();
    }

    /**
     * ユーザークレームに変換
     *
     * @param record jOOQレコード
     * @return {@link UserClaimsView}
     */
    default UserClaimsView toUserClaims(Record record) {
        return UserClaimsView.builder()
                .userId(record.get(USERS.ID))
                .email(record.get(USERS.EMAIL))
                .familyName(record.get(USER_PROFILES.FAMILY_NAME))
                .givenName(record.get(USER_PROFILES.GIVEN_NAME))
                .build();
    }

}
