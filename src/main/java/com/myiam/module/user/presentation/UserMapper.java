package com.myiam.module.user.presentation;

import com.myiam.module.user.application.model.command.ChangePasswordCommand;
import com.myiam.module.user.application.model.command.DisableUserCommand;
import com.myiam.module.user.application.model.command.RegisterUserCommand;
import com.myiam.module.user.application.model.command.UpdateProfileCommand;
import com.myiam.module.user.application.model.view.UserDetailView;
import com.myiam.module.user.presentation.model.request.ChangePasswordRequest;
import com.myiam.module.user.presentation.model.request.RegisterUserRequest;
import com.myiam.module.user.presentation.model.request.UpdateProfileRequest;
import com.myiam.module.user.presentation.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * ユーザーマッパー
 */
@Mapper(componentModel = "spring")
interface UserMapper {

    /**
     * {@code RegisterUserRequest} を {@code RegisterUserCommand} に変換する
     *
     * @param request 変換する {@code RegisterUserRequest}
     * @return {@code RegisterUserCommand}
     */
    RegisterUserCommand toRegisterUserCommand(RegisterUserRequest request);

    /**
     * {@code ChangePasswordRequest} を {@code ChangePasswordCommand} に変換する
     *
     * @param userId  ユーザー ID
     * @param request 変換する {@code ChangePasswordRequest}
     * @return {@code ChangePasswordCommand}
     */
    ChangePasswordCommand toChangePasswordCommand(UUID userId, ChangePasswordRequest request);

    /**
     * {@code UpdateProfileRequest} を {@code UpdateProfileCommand} に変換する
     *
     * @param userId  ユーザー ID
     * @param request 変換する {@code UpdateProfileRequest}
     * @return {@code UpdateProfileCommand}
     */
    UpdateProfileCommand toUpdateProfileCommand(UUID userId, UpdateProfileRequest request);

    /**
     * ユーザー ID を {@code DisableUserCommand} に変換する
     *
     * @param userId ユーザー ID
     * @return {@code DisableUserCommand}
     */
    @Mapping(target = "userId", source = "userId")
    DisableUserCommand toDisableCommand(UUID userId);

    /**
     * {@code UserDetailView} を {@code UserResponse} に変換する
     *
     * @param userView 変換する {@code UserDetailView}
     * @return {@code UserResponse}
     */
    UserResponse toUserResponse(UserDetailView userView);
}
