package com.myiam.module.identity.domain.user;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.module.identity.domain.user.port.PasswordHasher;
import com.myiam.module.identity.domain.user.vo.Email;
import com.myiam.module.identity.domain.user.vo.HashedPassword;
import com.myiam.module.identity.domain.user.vo.RawPassword;
import com.myiam.module.identity.domain.user.vo.UserProfile;
import com.myiam.module.identity.event.PasswordChanged;
import com.myiam.module.identity.event.ProfileUpdated;
import com.myiam.module.identity.event.UserDisabled;
import com.myiam.module.identity.event.UserLocked;
import com.myiam.module.identity.event.UserLoggedIn;
import com.myiam.module.identity.event.UserRegistered;
import com.myiam.module.identity.event.UserUnlocked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link User} の単体テスト。<br />
 * Spring もインフラも使わない、純粋なドメインの振る舞いの検証。
 */
class UserTest {

    /** テスト用のパスワードハッシャー ※ハッシュは接頭辞を付けるだけ */
    private static final PasswordHasher HASHER = new PasswordHasher() {
        @Override
        public HashedPassword hash(RawPassword rawPassword) {
            return new HashedPassword("{test}" + rawPassword.value());
        }

        @Override
        public boolean matches(RawPassword rawPassword, HashedPassword hashedPassword) {
            return hashedPassword.value().equals("{test}" + rawPassword.value());
        }
    };

    private static final Email EMAIL = new Email("test@example.com");
    private static final RawPassword PASSWORD = new RawPassword("password01");
    private static final UserProfile PROFILE = new UserProfile("山田", "太郎");

    /** 登録済みのユーザーを作る ※登録イベントは消費済みにしておく */
    private static User registeredUser() {
        User user = User.register(EMAIL, PASSWORD, PROFILE, HASHER);
        user.pullEvents();
        return user;
    }

    // ============================== register ==============================

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("登録すると有効状態で UserRegistered イベントが積まれる")
        void registersEnabledUserWithEvent() {
            User user = User.register(EMAIL, PASSWORD, PROFILE, HASHER);

            var snapshot = user.toSnapshot();
            assertThat(snapshot.status().enabled()).isTrue();
            assertThat(snapshot.status().passwordLocked()).isFalse();
            assertThat(snapshot.password().value()).isEqualTo("{test}password01");
            assertThat(snapshot.version()).isZero();

            assertThat(user.pullEvents())
                    .singleElement()
                    .isInstanceOf(UserRegistered.class)
                    .extracting("userId").isEqualTo(user.id());
        }

        @Test
        @DisplayName("pullEvents は一度取り出すと空になる")
        void pullEventsDrainsQueue() {
            User user = User.register(EMAIL, PASSWORD, PROFILE, HASHER);

            assertThat(user.pullEvents()).hasSize(1);
            assertThat(user.pullEvents()).isEmpty();
        }
    }

    // ============================== changePassword ==============================

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("旧パスワードが一致すれば変更され PasswordChanged が積まれる")
        void changesPasswordWhenOldMatches() {
            User user = registeredUser();

            user.changePassword(PASSWORD, new RawPassword("newpass01"), HASHER);

            assertThat(user.toSnapshot().password().value()).isEqualTo("{test}newpass01");
            assertThat(user.pullEvents()).singleElement().isInstanceOf(PasswordChanged.class);
        }

        @Test
        @DisplayName("旧パスワードが不一致なら PASSWORD_NOT_MATCHED")
        void rejectsWrongOldPassword() {
            User user = registeredUser();

            assertThatThrownBy(() -> user.changePassword(new RawPassword("wrongpass1"), new RawPassword("newpass01"), HASHER))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(errorCodeOf(ex)).isEqualTo(UserErrorCode.PASSWORD_NOT_MATCHED.name()));

            assertThat(user.toSnapshot().password().value()).isEqualTo("{test}password01");
            assertThat(user.pullEvents()).isEmpty();
        }

        @Test
        @DisplayName("無効化済みユーザーは変更できない")
        void rejectsWhenDisabled() {
            User user = registeredUser();
            user.disable();
            user.pullEvents();

            assertThatThrownBy(() -> user.changePassword(PASSWORD, new RawPassword("newpass01"), HASHER))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(errorCodeOf(ex)).isEqualTo(UserErrorCode.USER_ALREADY_DISABLED.name()));
        }
    }

    // ============================== updateProfile ==============================

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("プロフィールが差し替わり ProfileUpdated が積まれる")
        void updatesProfile() {
            User user = registeredUser();

            user.updateProfile(new UserProfile("佐藤", "花子"));

            assertThat(user.toSnapshot().profile()).isEqualTo(new UserProfile("佐藤", "花子"));
            assertThat(user.pullEvents()).singleElement().isInstanceOf(ProfileUpdated.class);
        }

        @Test
        @DisplayName("無効化済みユーザーは更新できない")
        void rejectsWhenDisabled() {
            User user = registeredUser();
            user.disable();

            assertThatThrownBy(() -> user.updateProfile(new UserProfile("佐藤", "花子")))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ============================== disable ==============================

    @Nested
    @DisplayName("disable")
    class Disable {

        @Test
        @DisplayName("無効化すると enabled=false で UserDisabled が積まれる")
        void disablesUser() {
            User user = registeredUser();

            user.disable();

            assertThat(user.toSnapshot().status().enabled()).isFalse();
            assertThat(user.pullEvents()).singleElement().isInstanceOf(UserDisabled.class);
        }

        @Test
        @DisplayName("二重無効化は USER_ALREADY_DISABLED")
        void rejectsDoubleDisable() {
            User user = registeredUser();
            user.disable();

            assertThatThrownBy(user::disable)
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(errorCodeOf(ex)).isEqualTo(UserErrorCode.USER_ALREADY_DISABLED.name()));
        }
    }

    // ============================== recordLogin ==============================

    @Nested
    @DisplayName("recordLogin")
    class RecordLogin {

        @Test
        @DisplayName("最終ログイン日時が記録され UserLoggedIn が積まれる")
        void recordsLastLogin() {
            User user = registeredUser();
            assertThat(user.toSnapshot().status().lastLoginAt()).isNull();

            user.recordLogin();

            assertThat(user.toSnapshot().status().lastLoginAt()).isNotNull();
            assertThat(user.pullEvents()).singleElement().isInstanceOf(UserLoggedIn.class);
        }
    }

    // ============================== lockPassword / unlockPassword ==============================

    @Nested
    @DisplayName("lockPassword / unlockPassword")
    class Lock {

        @Test
        @DisplayName("ロックすると passwordLocked=true で UserLocked が積まれる")
        void locks() {
            User user = registeredUser();

            user.lockPassword();

            assertThat(user.toSnapshot().status().passwordLocked()).isTrue();
            assertThat(user.pullEvents()).singleElement().isInstanceOf(UserLocked.class);
        }

        @Test
        @DisplayName("ロックは冪等：二回目はイベントを積まない")
        void lockIsIdempotent() {
            User user = registeredUser();
            user.lockPassword();
            user.pullEvents();

            user.lockPassword();

            assertThat(user.toSnapshot().status().passwordLocked()).isTrue();
            assertThat(user.pullEvents()).isEmpty();
        }

        @Test
        @DisplayName("解除すると passwordLocked=false で UserUnlocked が積まれる")
        void unlocks() {
            User user = registeredUser();
            user.lockPassword();
            user.pullEvents();

            user.unlockPassword();

            assertThat(user.toSnapshot().status().passwordLocked()).isFalse();
            assertThat(user.pullEvents()).singleElement().isInstanceOf(UserUnlocked.class);
        }

        @Test
        @DisplayName("ロックされていない状態の解除は何もしない")
        void unlockIsIdempotent() {
            User user = registeredUser();

            user.unlockPassword();

            assertThat(user.pullEvents()).isEmpty();
        }
    }

    // ============================== snapshot ==============================

    @Nested
    @DisplayName("snapshot")
    class Snapshot {

        @Test
        @DisplayName("スナップショット → 復元 → スナップショット が往復できる")
        void roundTrips() {
            User original = registeredUser();
            original.recordLogin();
            original.lockPassword();

            User restored = new UserFactory().restore(original.toSnapshot());

            assertThat(restored.toSnapshot()).isEqualTo(original.toSnapshot());
            assertThat(restored).isEqualTo(original);
            assertThat(restored.pullEvents()).isEmpty();
        }
    }

    // ============================== ヘルパー ==============================

    private static String errorCodeOf(Throwable ex) {
        return ((BusinessException) ex).getErrorDetail().errorCode().code();
    }
}
