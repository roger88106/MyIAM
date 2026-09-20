package com.myiam.module.identity.domain.user.vo;

import lombok.Builder;
import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Instant;
import java.util.Objects;

/**
 * ユーザーのステータス
 *
 * @param enabled 有効ユーザー
 * @param createdAt 作成日時
 * @param lastLoginAt 最終ログイン日時
 * @param passwordChangedAt パスワード変更日時
 * @param passwordLocked パスワードロック中
 */
@ValueObject
@Builder(toBuilder = true)
public record UserStatus(boolean enabled, Instant createdAt, Instant lastLoginAt, Instant passwordChangedAt,
                         boolean passwordLocked) {

    /**
     * ユーザーのステータス
     *
     * @param enabled 有効ユーザー
     * @param createdAt 作成日時
     * @param lastLoginAt 最終ログイン日時
     * @param passwordChangedAt パスワード変更日時
     * @param passwordLocked パスワードロック中
     */
    public UserStatus {
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(passwordChangedAt, "passwordChangedAt must not be null");
    }

    // ============================== 状態更新メソッド ==============================

    /**
     * 新規ユーザーのステータスを作成する
     *
     * @return 新規ユーザーのステータス
     */
    public static UserStatus newlyCreated() {
        Instant now = Instant.now();
        return UserStatus.builder()
                .enabled(true)
                .createdAt(now)
                .passwordChangedAt(now)
                .build();
    }

    /**
     * パスワード変更を記録する
     *
     * @return パスワード変更後のステータス
     */
    public UserStatus recordPasswordChange() {
        Instant now = Instant.now();
        return this.toBuilder()
                .passwordChangedAt(now)
                .build();
    }

    /**
     * ユーザーを無効化する
     *
     * @return ユーザーを無効化した後のステータス
     */
    public UserStatus disableUser() {
        return this.toBuilder()
                .enabled(false)
                .build();
    }

    /**
     * ログインを記録する
     *
     * @return 最終ログイン日時を更新した後のステータス
     */
    public UserStatus recordLogin() {
        return this.toBuilder()
                .lastLoginAt(Instant.now())
                .build();
    }

    /**
     * パスワードをロックする
     *
     * @return ロック後のステータス
     */
    public UserStatus lockPassword() {
        return this.toBuilder()
                .passwordLocked(true)
                .build();
    }

    /**
     * パスワードのロックを解除する
     *
     * @return ロック解除後のステータス
     */
    public UserStatus unlockPassword() {
        return this.toBuilder()
                .passwordLocked(false)
                .build();
    }
}
