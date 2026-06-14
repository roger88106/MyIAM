package com.myiam.common.model.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * ドメインイベント
 */
public interface DomainEvent extends org.jmolecules.event.types.DomainEvent {
    /**
     * @return イベント ID
     */
    UUID eventId();

    /**
     * @return イベント発生時刻
     */
    Instant occurredAt();
}
