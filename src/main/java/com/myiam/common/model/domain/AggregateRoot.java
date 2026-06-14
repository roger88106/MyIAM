package com.myiam.common.model.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * アグリゲートルート
 */
public abstract class AggregateRoot {

    /**
     * イベントリスト
     */
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * イベント登録
     * @param event イベント
     */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * イベント取得
     * @return イベントリスト
     */
    public List<DomainEvent> pullEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}