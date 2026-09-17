package com.myiam.module.identity.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：ユーザーのパスワードロックが解除された
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record UserUnlocked(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * ユーザーロック解除イベントを生成する
     *
     * @param userId ユーザー ID
     * @return ユーザーロック解除イベント
     */
    public static UserUnlocked of(UUID userId) {
        return new UserUnlocked(UUID.randomUUID(), Instant.now(), userId);
    }
}
