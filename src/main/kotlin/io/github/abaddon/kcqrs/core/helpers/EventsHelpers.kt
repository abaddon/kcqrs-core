package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent

fun <TAggregate : IAggregate> Iterable<IDomainEvent>.foldEvents(initial: TAggregate, version: Long): TAggregate {
    val numUniqueAggregateId = this.distinctBy { it.aggregateId }.size
    require(this.groupBy { it.aggregateId }.keys.size <= 1) { "Each event should be part of the same aggregate. Found $numUniqueAggregateId aggregateIds in ${this.count()} events" }
    return this.fold(initial) { acc, ev ->
        if (version > acc.version) {
            acc.applyEvent(ev) as TAggregate
        } else {
            acc
        }
    }
}

