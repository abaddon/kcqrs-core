package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


interface IProjection {
    val key: IProjectionKey
    val lastProcessedEvent: ConcurrentHashMap<String, Long>
    val lastUpdated: Instant?


    fun applyEvent(event: IDomainEvent): IProjection

    fun withPosition(event: IDomainEvent): IProjection

    fun lastProcessedPositionOf(aggregateType: String): Long {
        return lastProcessedEvent[aggregateType] ?: 0
    }

}