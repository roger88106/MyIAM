package com.myiam.module.permission.event;

import com.myiam.common.model.domain.DomainEvent;

/**
 * 権限ドメインのイベント
 */
public sealed interface PermissionEvent extends DomainEvent
        permits RoleAssigned, RoleRevoked {

    String subject();
}
