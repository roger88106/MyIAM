package com.myiam.module.identity.presentation;

import com.myiam.module.identity.application.UserCommandService;
import com.myiam.module.identity.application.UserQueryService;
import com.myiam.module.identity.presentation.model.request.ChangePasswordRequest;
import com.myiam.module.identity.presentation.model.request.RegisterUserRequest;
import com.myiam.module.identity.presentation.model.request.UpdateProfileRequest;
import com.myiam.module.identity.presentation.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * ユーザーコントローラー
 */
@RestController
@RequestMapping("/api/users")
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

// ============================== GET ==============================

    /**
     * ユーザー取得
     *
     * @param userId ユーザーID
     * @return Http 200 : {@link UserResponse}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        // ユーザー取得
        var userView = queryService.getUserById(userId);

        // レスポンス設定
        return ResponseEntity.ok(mapper.toUserResponse(userView));
    }

// ============================== POST ==============================

    /**
     * ユーザー登録
     *
     * @param request ユーザー登録リクエスト
     * @return Http 201 : VOID
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody @Validated RegisterUserRequest request
    ) {
        // ユーザー登録
        UUID userId = commandService.registerUser(mapper.toRegisterUserCommand(request));

        // ロケーション取得
        URI location = MvcUriComponentsBuilder
                .fromMethodCall(MvcUriComponentsBuilder.on(UserController.class).getUserById(userId))
                .build()
                .toUri();

        // レスポンス設定
        return ResponseEntity.created(location).build();
    }

// ============================== PUT ==============================

    /**
     * パスワード変更
     *
     * @param userId ユーザー ID
     * @param request パスワード変更リクエスト
     * @return Http 204 : VOID
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasAuthority('user:write')")
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
    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasAuthority('user:write')")
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
    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> disableUser(@PathVariable UUID userId) {
        // ユーザー無効化
        commandService.disableUser(mapper.toDisableCommand(userId));

        // レスポンス設定
        return ResponseEntity.noContent().build();
    }

// ============================== PATCH ==============================


// ============================== DELETE ==============================



}
