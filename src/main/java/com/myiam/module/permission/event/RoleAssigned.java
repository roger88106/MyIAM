package com.myiam.module.permission.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：サブジェクトにロールが割り当てられた
 *
 * @param eventId    イベント ID
 * @param occurredAt イベント発生時刻
 * @param subject    サブジェクト
 * @param role       ロール
 */
@DomainEvent
public record RoleAssigned(UUID eventId, Instant occurredAt, String subject, String role) implements PermissionEvent {

    /**
     * ロール割当イベントを生成する
     *
     * @param subject サブジェクト
     * @param role    ロール
     * @return ロール割当イベント
     */
    public static RoleAssigned of(String subject, String role) {
        return new RoleAssigned(UUID.randomUUID(), Instant.now(), subject, role);
    }
}
