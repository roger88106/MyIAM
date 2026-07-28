package com.myiam.module.user.domain.user;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.common.model.domain.AggregateRoot;
import com.myiam.module.user.event.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jmolecules.ddd.annotation.Identity;

import java.util.List;
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
     * ユーザーのメールアドレス
     */
    private Email email;

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

    /**
     * バージョン
     */
    private final long version;

// ============================== コンストラクタ ==============================

    /**
     * ユーザーの全属性コンストラクタ
     *
     * @param id       ユーザー ID
     * @param email    ユーザーのメールアドレス
     * @param password パスワード
     * @param profile  ユーザープロファイル
     * @param status   ユーザーステータス
     * @param version  バージョン
     */
    private User(UUID id, Email email, HashedPassword password, UserProfile profile, UserStatus status, long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.version = version;
    }

// ============================== Getter ==============================

    /**
     * @return ユーザー ID
     */
    public UUID id() {
        return id;
    }

// ============================== ドメインの振る舞い ==============================

    /**
     * 登録ユーザを作成する
     *
     * @param email          ユーザーのメールアドレス
     * @param rawPassword    パスワード
     * @param profile        ユーザープロファイル
     * @param passwordHasher パスワードハッシュ化処理クラス
     * @return 登録ユーザ
     */
    public static User register(@NonNull Email email, @NonNull RawPassword rawPassword, @NonNull UserProfile profile, @NonNull PasswordHasher passwordHasher) {

        // 新規ユーザーID取得
        UUID id = UUID.randomUUID();

        // 登録ユーザ作成
        User user = new User(id, email, passwordHasher.hash(rawPassword), profile, UserStatus.newlyCreated(), 0);

        // イベント登録：ユーザーが登録された
        user.registerEvent(UserRegistered.of(id));

        return user;
    }


    /**
     * パスワードを変更する
     *
     * @param oldPassword    古いパスワード
     * @param newPassword    新しいパスワード
     * @param passwordHasher パスワードハッシュ化処理クラス
     */
    public void changePassword(@NonNull RawPassword oldPassword, @NonNull RawPassword newPassword, @NonNull PasswordHasher passwordHasher) {
        // ユーザーは既に無効化されている場合、エラーとする
        throwIfDisabled();

        // 古いパスワードが一致していない場合、エラーとする
        if (!passwordHasher.matches(oldPassword, password)) {
            throw BusinessException.of(UserErrorCode.PASSWORD_NOT_MATCHED.getCode());
        }

        // パスワード変更
        this.password = passwordHasher.hash(newPassword);

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
     * @param email    ユーザーのメールアドレス
     * @param password パスワード
     * @param profile  ユーザープロファイル
     * @param status   ユーザーステータス
     * @param version  バージョン
     */
    @Builder
    public record Snapshot(UUID id, Email email, HashedPassword password, UserProfile profile,
                           UserStatus status, long version) {
    }

    /**
     * スナップショットを取得する
     *
     * @return ユーザースナップショット
     */
    public Snapshot toSnapshot() {
        return Snapshot.builder()
                .id(id)
                .email(email)
                .password(password)
                .profile(profile)
                .status(status)
                .version(version)
                .build();
    }

    /**
     * ユーザー
     *
     * @param snapshot ユーザースナップショット
     */
    User(Snapshot snapshot) {
        this(snapshot.id(),
                snapshot.email(),
                snapshot.password(),
                snapshot.profile(),
                snapshot.status(),
                snapshot.version());
    }

// ============================== プライベートメソッド ==============================

    /**
     * 無効化されている場合に例外をスローする
     *
     * @throws BusinessException 無効化されている場合
     */
    private void throwIfDisabled() {
        if (!status.enabled()) {
            throw BusinessException.of(UserErrorCode.USER_ALREADY_DISABLED.getCode(), List.of(id));
        }
    }

}