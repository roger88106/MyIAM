package com.myiam.module.user.application;

import com.myiam.common.error.BusinessException;
import com.myiam.module.user.application.model.command.ChangePasswordCommand;
import com.myiam.module.user.application.model.command.DisableUserCommand;
import com.myiam.module.user.application.model.command.RegisterUserCommand;
import com.myiam.module.user.application.model.command.UpdateProfileCommand;
import com.myiam.module.user.domain.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ユーザーコマンドサービス
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {

    /**
     * ユーザーリポジトリ
     */
    private final UserRepository userRepository;

    /**
     * パスワードハッシュ化処理クラス
     */
    private final PasswordHasher passwordHasher;

    /**
     * ユーザー登録
     *
     * @param command ユーザー登録コマンド
     * @return ユーザー ID
     */
    @Transactional
    public UUID registerUser(RegisterUserCommand command) {
        // VO作成
        var identity = new UserIdentity(command.username(), command.email());
        var rawPassword = new RawPassword(command.password());
        var profile = new UserProfile(command.profile().familyName(), command.profile().givenName());

        // ユーザーが既に存在する場合はエラーとする
        if (userRepository.existsByIdentity(identity)) {
            String identityValue = identity.username() != null ? identity.username() : identity.email();
            throw BusinessException.of("user is already exists: %s".formatted(identityValue), UserErrorCode.USER_ALREADY_EXISTS, identityValue);
        }

        // 登録ユーザ作成
        User user = User.register(identity, rawPassword, profile, passwordHasher);

        // ユーザー保存
        userRepository.save(user);

        return user.id();
    }

    /**
     * パスワード変更
     *
     * @param command パスワード変更コマンド
     */
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        // VO作成
        var oldPassword = new RawPassword(command.oldPassword());
        var newPassword = new RawPassword(command.newPassword());

        // ユーザー取得
        User user = findUserById(command.userId());

        // パスワード変更
        user.changePassword(oldPassword, newPassword, passwordHasher);

        // ユーザー保存
        userRepository.save(user);
    }

    /**
     * プロファイル更新
     *
     * @param command プロファイル更新コマンド
     */
    @Transactional
    public void updateProfile(UpdateProfileCommand command) {
        // VO作成
        var profile = new UserProfile(command.familyName(), command.givenName());

        // ユーザー取得
        var user = findUserById(command.userId());

        // プロファイル更新
        user.updateProfile(profile);

        // ユーザー保存
        userRepository.save(user);
    }

    /**
     * ユーザー無効化
     *
     * @param command ユーザー無効化コマンド
     */
    @Transactional
    public void disableUser(DisableUserCommand command) {

        // ユーザー取得
        var user = findUserById(command.userId());

        // ユーザー無効化
        user.disable();

        // ユーザー保存
        userRepository.save(user);
    }

// ============================== プライベートメソッド ==============================

    /**
     * ユーザー取得
     *
     * @param userId ユーザー ID
     * @return ユーザー ドメインモデル
     */
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of("user %s is not found".formatted(userId), UserErrorCode.USER_NOT_FOUND));
    }
}
