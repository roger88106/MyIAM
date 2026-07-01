package com.myiam.module.user.infrastructure;

import com.myiam.jooq.user.tables.records.UsersRecord;
import com.myiam.module.user.application.model.result.UserClaims;
import com.myiam.module.user.application.model.result.UserCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static com.myiam.jooq.user.Tables.USERS;
import static com.myiam.jooq.user.Tables.USER_PROFILES;

/**
 * ユーザーリポジトリマッパー
 */
@Mapper(componentModel = "spring")
interface UserRepositoryMapper {

    /**
     * {@link UsersRecord} を {@link UserCredential} に変換する
     *
     * @param record 変換する {@code UsersRecord}
     * @return {@code UserCredential}
     */
    @Mapping(target = "userId", source = "id")
    UserCredential toUserCredential(UsersRecord record);

// ============================== Joinのテーブル用の変換メソッド ==============================

    /**
     * ユーザークレームに変換
     *
     * @param record jOOQレコード
     * @return {@link UserClaims}
     */
    default UserClaims toUserClaims(org.jooq.Record record) {
        return UserClaims.builder()
                .userId(record.get(USERS.ID))
                .email(record.get(USERS.EMAIL))
                .familyName(record.get(USER_PROFILES.FAMILY_NAME))
                .givenName(record.get(USER_PROFILES.GIVEN_NAME))
                .build();
    }

}
