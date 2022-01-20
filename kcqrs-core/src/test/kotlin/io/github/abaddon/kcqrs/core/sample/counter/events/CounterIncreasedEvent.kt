package io.github.abaddon.kcqrs.core.sample.counter.events

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.sample.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.core.sample.counter.entities.CounterAggregateRoot
import java.util.*

data class CounterIncreasedEvent(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    val value: Int
) : DomainEvent { //CounterAggregateRoot
    override val version: Int = 1
    override val aggregateType: String = CounterAggregateRoot.javaClass.simpleName
    override val header: EventHeader = EventHeader.create("CounterAggregateRoot")

    companion object {
        fun create(aggregateId: CounterAggregateId, value: Int): CounterIncreasedEvent =
            CounterIncreasedEvent(UUID.randomUUID(), aggregateId, value)
    }
}