package com.myiam.module.identity.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：ユーザーがログインした
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record UserLoggedIn(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * ユーザーログインイベントを生成する
     *
     * @param userId ユーザー ID
     * @return ユーザーログインイベント
     */
    public static UserLoggedIn of(UUID userId) {
        return new UserLoggedIn(UUID.randomUUID(), Instant.now(), userId);
    }
}
