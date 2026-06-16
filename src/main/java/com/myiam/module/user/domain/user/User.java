package com.myiam.module.user.domain.user;

import com.myiam.common.error.BusinessException;
import com.myiam.common.model.domain.AggregateRoot;
import com.myiam.module.user.event.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;
import java.util.UUID;

/**
 * ユーザードメインの Aggregate Root
 */
@org.jmolecules.ddd.annotation.AggregateRoot
@EqualsAndHashCode(of = "id")
public class User extends AggregateRoot {

// ============================== フィールド定義 ==============================

    /**
     * ユーザー ID
     */
    @Identity
    private final UUID id;

    /**
     * ユーザーの識別情報
     */
    private UserIdentity identity;

    /**
     * パスワード
     */
    private HashedPassword password;

    /**
     * ユーザープロファイル
     */
    private UserProfile profile;

    /**
     * ユーザーステータス
     */
    private UserStatus status;

// ============================== コンストラクタ ==============================

    /**
     * ユーザーの全属性コンストラクタ
     *
     * @param id       ユーザー ID
     * @param identity ユーザーの識別情報
     * @param password パスワード
     * @param profile  ユーザープロファイル
     * @param status   ユーザーステータス
     */
    private User(UUID id, UserIdentity identity, HashedPassword password, UserProfile profile, UserStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.identity = Objects.requireNonNull(identity, "identity must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

// ============================== ドメインの振る舞い ==============================

    /**
     * 登録ユーザを作成する
     *
     * @param identity    ユーザーの識別情報
     * @param rawPassword パスワード
     * @param profile     ユーザープロファイル
     * @return 登録ユーザ
     */
    public static User register(@NonNull UserIdentity identity, @NonNull RawPassword rawPassword, @NonNull UserProfile profile, @NonNull PasswordHasher passwordHasher) {

        // 新規ユーザーID取得
        UUID id = UUID.randomUUID();

        // 登録ユーザ作成
        User user = new User(id, identity, passwordHasher.hash(rawPassword), profile, UserStatus.newlyCreated());

        // イベント登録：ユーザーが登録された
        user.registerEvent(UserRegistered.of(id));

        return user;
    }


    /**
     * パスワードを変更する
     *
     * @param password 新しいパスワード
     */
    public void changePassword(@NonNull RawPassword password, @NonNull PasswordHasher passwordHasher) {
        // ユーザーは既に無効化されている場合、エラーとする
        throwIfDisabled();

        // パスワード変更
        this.password = passwordHasher.hash(password);

        // ステータス更新
        status = status.recordPasswordChange();

        // イベント登録：パスワードが変更された
        registerEvent(PasswordChanged.of(id));
    }

    /**
     * プロフィールを変更する
     *
     * @param profile 新しいユーザープロファイル
     */
    public void updateProfile(@NonNull UserProfile profile) {
        // ユーザーは既に無効化されている場合、エラーとする
        throwIfDisabled();

        // プロフィール変更
        this.profile = profile;

        // イベント登録：プロフィールが変更された
        registerEvent(ProfileUpdated.of(id));
    }

    /**
     * ユーザーを無効化する
     */
    public void disable() {
        // ユーザーは既に無効化されている場合、エラーとする
        throwIfDisabled();

        // ユーザー無効化
        status = status.disableUser();

        // イベント登録：ユーザーが無効化された
        registerEvent(UserDisabled.of(id));
    }

// ============================== ドメインイベント関連処理 ==============================

    /**
     * ドメインイベントを登録する
     *
     * @param event ユーザードメインのイベント
     */
    void registerEvent(UserEvent event) {
        super.registerEvent(event);
    }

// ============================== スナップショット関連処理 ==============================

    /**
     * ユーザースナップショット
     *
     * @param id       ユーザー ID
     * @param identity ユーザーの識別情報
     * @param password パスワード
     * @param profile  ユーザープロファイル
     * @param status   ユーザーステータス
     */
    @Builder
    public record Snapshot(UUID id, UserIdentity identity, HashedPassword password, UserProfile profile,
                           UserStatus status) {
    }

    /**
     * スナップショットを取得する
     *
     * @return ユーザースナップショット
     */
    public Snapshot toSnapshot() {
        return Snapshot.builder()
                .id(id)
                .identity(identity)
                .password(password)
                .profile(profile)
                .status(status)
                .build();
    }

    /**
     * ユーザー
     *
     * @param snapshot ユーザースナップショット
     */
    User(Snapshot snapshot) {
        this(snapshot.id(),
                snapshot.identity(),
                snapshot.password(),
                snapshot.profile(),
                snapshot.status());
    }

// ============================== プライベートメソッド ==============================

    /**
     * 無効化されている場合に例外をスローする
     */
    private void throwIfDisabled() throws BusinessException {
        if (!status.enabled()) {
            throw BusinessException.of("User is already disabled", UserErrorCode.USER_ALREADY_DISABLED, id);
        }
    }

}