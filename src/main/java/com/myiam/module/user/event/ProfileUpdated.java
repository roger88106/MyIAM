package com.myiam.module.user.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：プロフィールが更新された
 *
 * @param eventId    イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId     ユーザー ID
 */
@DomainEvent
public record ProfileUpdated(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * プロフィール更新イベントを生成する
     *
     * @param userId ユーザー ID
     * @return プロフィール更新イベント
     */
    public static ProfileUpdated of(UUID userId) {
        return new ProfileUpdated(UUID.randomUUID(), Instant.now(), userId);
    }
}
