package com.myiam.module.user.infrastructure;

import com.myiam.common.error.CommonErrorCode;
import com.myiam.common.error.exception.BusinessException;
import com.myiam.jooq.user.tables.UserProfiles;
import com.myiam.jooq.user.tables.records.UserProfilesRecord;
import com.myiam.jooq.user.tables.records.UsersRecord;
import com.myiam.module.user.domain.user.*;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.myiam.jooq.user.tables.Users.USERS;
import static org.jooq.impl.DSL.selectFrom;

/**
 * ユーザーのリポジトリ
 */
@Repository
@RequiredArgsConstructor
class JooqUserRepository implements UserRepository {

    /**
     * jOOQ DSL Context
     */
    private final DSLContext dsl;

    /**
     * Spring イベントパブリッシャー
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * ユーザーファクトリ
     */
    private final UserFactory userFactory;

    /**
     * ユーザーの識別情報からユーザーが存在するかどうかを判定する。
     *
     * @param email ユーザーのメールアドレス
     * @return 存在する場合 {@code true}, 存在しない場合 {@code false}
     */
    @Override
    public boolean existsByIdentifier(Email email) {
        return dsl.fetchExists(
                selectFrom(USERS)
                        .where(USERS.EMAIL.eq(email.value()))
        );
    }

    /**
     * ユーザー ID からユーザーを取得する。
     *
     * @param id ユーザー ID
     * @return ユーザー
     */
    @Override
    public Optional<User> findById(UUID id) {
        // ユーザー取得
        return dsl.select()
                .from(USERS)
                .join(UserProfiles.USER_PROFILES)
                .on(USERS.ID.eq(UserProfiles.USER_PROFILES.ID))
                .where(USERS.ID.eq(id))
                .fetchOptional()
                // スナップショットに変換する
                .map(record -> toSnapshot(record.into(UsersRecord.class), record.into(UserProfilesRecord.class)))
                // スナップショットからドメインに変換する
                .map(userFactory::restore);
    }

    /**
     * ユーザーを追加する。
     *
     * @param user ユーザー
     */
    @Override
    public void add(User user) {
        // スナップショット取得
        var snapshot = user.toSnapshot();

        // レコード変換処理
        var usersRecord = toUsersRecord(snapshot);
        var profilesRecord = toUserProfilesRecord(snapshot);

        // 登録項目設定
        usersRecord
                // 登録日時
                .setCreatedAt(snapshot.status().createdAt())
                // 登録者
                .setCreatedBy("system");

        // ユーザー登録
        dsl.insertInto(USERS)
                .set(usersRecord)
                .execute();

        // ユーザープロファイル登録
        dsl.insertInto(UserProfiles.USER_PROFILES)
                .set(profilesRecord)
                .execute();

        // イベント発行
        user.pullEvents().forEach(eventPublisher::publishEvent);
    }

    /**
     * ユーザーを更新する。
     *
     * @param user ユーザー スナップショット
     */
    @Override
    public void save(User user) {
        // スナップショット取得
        var snapshot = user.toSnapshot();

        // レコード変換処理
        var usersRecord = toUsersRecord(snapshot);
        var profilesRecord = toUserProfilesRecord(snapshot);

        // 現在のバージョン
        var nowVersion = snapshot.version();

        // 更新項目設定
        usersRecord
                // 更新日時
                .setUpdatedAt(Instant.now())
                // 更新者
                .setUpdatedBy("system") // ToDo: ユーザーIDを設定する
                // バージョン
                .setVersion(nowVersion + 1);


        // 排除項目設定
        usersRecord.touched(USERS.CREATED_AT, false);
        usersRecord.touched(USERS.CREATED_BY, false);
        profilesRecord.touched(USERS.CREATED_AT, false);
        profilesRecord.touched(USERS.CREATED_BY, false);

        // ユーザーのアグリゲートルート更新
        int affected = dsl.update(USERS)
                .set(usersRecord)
                .where(USERS.ID.eq(usersRecord.getId()))
                .and(USERS.VERSION.eq(nowVersion))
                .execute();

        // 楽観排他された場合、エラーをスローする
        if (affected == 0) {
            throw BusinessException.of(CommonErrorCode.CONCURRENT_MODIFICATION.getCode());
        }

        // プロファイル更新
        dsl.update(UserProfiles.USER_PROFILES)
                .set(profilesRecord)
                .where(UserProfiles.USER_PROFILES.ID.eq(profilesRecord.getId()))
                .execute();

        // イベント発行
        user.pullEvents().forEach(eventPublisher::publishEvent);
    }

// ============================== ファクトリ ==============================

    /**
     * ユーザーのスナップショットに変換
     *
     * @param users        ユーザーレコード
     * @param userProfiles ユーザープロファイルレコード
     * @return ユーザーのスナップショット
     */
    private User.Snapshot toSnapshot(UsersRecord users, UserProfilesRecord userProfiles) {
        return new User.Snapshot(
                // ID
                users.getId(),
                // メールアドレス
                new Email(users.getEmail()),
                // パスワード情報
                new HashedPassword(users.getPassword()),
                // プロファイル情報
                new UserProfile(userProfiles.getFamilyName(), userProfiles.getGivenName()),
                // ステータス情報
                new UserStatus(users.getEnabled(), users.getCreatedAt(), users.getLastLoginAt(), users.getPasswordChangedAt(), users.getPasswordLocked()),
                // バージョン
                users.getVersion());
    }

    /**
     * ユーザーのテーブルレコードに変換 ※監察項目未設定
     *
     * @param user ユーザーのスナップショット
     * @return ユーザーレコード
     */
    private UsersRecord toUsersRecord(User.Snapshot user) {
        return new UsersRecord()
                // ID
                .setId(user.id())
                // ユーザー名
                .setUsername(user.email().value())
                // メールアドレス
                .setEmail(user.email().value())
                // パスワード
                .setPassword(user.password().value())
                // 有効フラグ
                .setEnabled(user.status().enabled())
                // 最終ログイン日時
                .setLastLoginAt(user.status().lastLoginAt())
                // パスワード変更日時
                .setPasswordChangedAt(user.status().passwordChangedAt())
                // パスワードロックフラグ
                .setPasswordLocked(user.status().passwordLocked())
                // バージョン
                .setVersion(user.version());
    }

    /**
     * ユーザープロファイルのテーブルレコードに変換 ※監察項目未設定
     *
     * @param user ユーザーのスナップショット
     * @return ユーザープロファイル レコード
     */
    private UserProfilesRecord toUserProfilesRecord(User.Snapshot user) {
        return new UserProfilesRecord()
                // ID
                .setId(user.id())
                // 姓
                .setFamilyName(user.profile().familyName())
                // 名
                .setGivenName(user.profile().givenName());
    }
}
