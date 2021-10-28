package io.github.abaddon.kcqrs.tests.counteraggregate.events

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId

data class CounterIncreasedEvent(
    override val aggregateId: CounterAggregateId,
    val value: Int
) : DomainEvent(aggregateId) { }