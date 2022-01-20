package io.github.abaddon.kcqrs.test.sample.counter.events

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateId
import java.util.*

data class CounterInitialisedEvent private constructor(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    override val version: Int = 1,
    override val aggregateType: String,
    override val header: EventHeader,
    val value: Int,

    ) : DomainEvent {
    constructor(aggregateId: CounterAggregateId, value: Int) : this(
        UUID.randomUUID(), aggregateId, 1, "CounterAggregateRoot",
        EventHeader.create("CounterAggregateRoot"), value
    )
}