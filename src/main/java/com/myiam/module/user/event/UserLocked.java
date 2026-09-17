package com.myiam.module.user.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：ユーザーのパスワードがロックされた
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record UserLocked(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * ユーザーロックイベントを生成する
     *
     * @param userId ユーザー ID
     * @return ユーザーロックイベント
     */
    public static UserLocked of(UUID userId) {
        return new UserLocked(UUID.randomUUID(), Instant.now(), userId);
    }
}
