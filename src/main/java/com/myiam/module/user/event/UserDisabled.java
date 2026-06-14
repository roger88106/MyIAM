package com.myiam.module.user.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：ユーザーが無効化された
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record UserDisabled(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * ユーザー無効化イベントを生成する
     *
     * @param userId ユーザー ID
     * @return ユーザー無効化イベント
     */
    public static UserDisabled of(UUID userId) {
        return new UserDisabled(UUID.randomUUID(), Instant.now(), userId);
    }
}
