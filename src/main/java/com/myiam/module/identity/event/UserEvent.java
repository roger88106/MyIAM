package com.myiam.module.identity.event;

import com.myiam.common.model.domain.DomainEvent;

import java.util.UUID;

/**
 * ユーザードメインのイベント
 */
public sealed interface UserEvent extends DomainEvent
        permits UserRegistered, UserDisabled, PasswordChanged, ProfileUpdated, UserLocked, UserUnlocked, UserLoggedIn {

    UUID userId();
}
