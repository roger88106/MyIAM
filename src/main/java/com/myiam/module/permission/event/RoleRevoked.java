package com.myiam.module.permission.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：サブジェクトからロールが解除された
 *
 * @param eventId    イベント ID
 * @param occurredAt イベント発生時刻
 * @param subject    サブジェクト
 * @param role       ロール
 */
@DomainEvent
public record RoleRevoked(UUID eventId, Instant occurredAt, String subject, String role) implements PermissionEvent {

    /**
     * ロール解除イベントを生成する
     *
     * @param subject サブジェクト
     * @param role    ロール
     * @return ロール解除イベント
     */
    public static RoleRevoked of(String subject, String role) {
        return new RoleRevoked(UUID.randomUUID(), Instant.now(), subject, role);
    }
}
