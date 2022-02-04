package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent

fun <TAggregate : IAggregate> Iterable<IDomainEvent>.foldEvents(initial: TAggregate): TAggregate {
    val numUniqueAggregateId=this.distinctBy{ it.aggregateId }.size
    require(this.groupBy { it.aggregateId }.keys.size <= 1){"Each event should be part of the same aggregate. Found $numUniqueAggregateId aggregateIds in ${this.count()} events"}
    return this.fold(initial) { acc, ev -> acc.applyEvent(ev) as TAggregate }
}

