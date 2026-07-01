package com.myiam.module.user.presentation;

import com.myiam.module.user.application.UserCommandService;
import com.myiam.module.user.application.UserQueryService;
import com.myiam.module.user.presentation.model.request.ChangePasswordRequest;
import com.myiam.module.user.presentation.model.request.RegisterUserRequest;
import com.myiam.module.user.presentation.model.request.UpdateProfileRequest;
import com.myiam.module.user.presentation.model.response.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ユーザーコントローラー
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    /**
     * ユーザーコマンドサービス
     */
    private final UserCommandService commandService;

    /**
     * ユーザークエリーサービス
     */
    private final UserQueryService queryService;

    /**
     * ユーザーマッパー
     */
    private final UserMapper mapper;

    // ToDo: 仮実装、API設計後再修正必須

// ============================== GET ==============================

// ============================== POST ==============================

    /**
     * ユーザー登録
     *
     * @param request ユーザー登録リクエスト
     * @return Http 200 : ユーザー登録レスポンス
     */
    @PostMapping
    public ResponseEntity<RegisterUserResponse> register(
            @RequestBody @Validated RegisterUserRequest request
    ) {
        // ユーザー登録
        UUID userId = commandService.registerUser(mapper.toRegisterUserCommand(request));

        // レスポンス設定
        // ToDo: ロケーション決定後、HTTP 201 に変更
        return ResponseEntity.ok().body(new RegisterUserResponse(userId));
    }

// ============================== PUT ==============================

// ============================== PATCH ==============================

    /**
     * パスワード変更
     *
     * @param userId ユーザー ID
     * @param request パスワード変更リクエスト
     * @return Http 204 : VOID
     */
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @RequestBody @Validated ChangePasswordRequest request
    ) {
        // パスワード変更
        commandService.changePassword(mapper.toChangePasswordCommand(userId, request));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }

    /**
     * プロファイル更新
     *
     * @param userId ユーザー ID
     * @param request プロファイル更新リクエスト
     * @return Http 204 : VOID
     */
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<Void> updateProfile(
            @PathVariable UUID userId,
            @RequestBody @Validated UpdateProfileRequest request
    ) {
        // プロファイル更新
        commandService.updateProfile(mapper.toUpdateProfileCommand(userId, request));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }

    /**
     * ユーザー無効化
     *
     * @param userId ユーザー ID
     * @return Http 204 : VOID
     */
    @PatchMapping("/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable UUID userId) {
        // ユーザー無効化
        commandService.disableUser(mapper.toDisableCommand(userId));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }

// ============================== DELETE ==============================



}
