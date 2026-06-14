package com.myiam.module.user.event;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * イベント：パスワードが変更された
 *
 * @param eventId イベント ID
 * @param occurredAt イベント発生時刻
 * @param userId ユーザー ID
 */
@DomainEvent
public record PasswordChanged(UUID eventId, Instant occurredAt, UUID userId) implements UserEvent {

    /**
     * パスワード変更イベントを生成する
     *
     * @param userId ユーザー ID
     * @return パスワード変更イベント
     */
    public static PasswordChanged of(UUID userId) {
        return new PasswordChanged(UUID.randomUUID(), Instant.now(), userId);
    }
}
