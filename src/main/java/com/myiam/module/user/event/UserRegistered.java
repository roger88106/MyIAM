package com.myiam.module.user.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：ユーザーが登録された
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record UserRegistered(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * ユーザー登録イベントを生成する
     *
     * @param userId ユーザー ID
     * @return ユーザー登録イベント
     */
    public static UserRegistered of(UUID userId) {
        return new UserRegistered(UUID.randomUUID(), Instant.now(), userId);
    }
}
