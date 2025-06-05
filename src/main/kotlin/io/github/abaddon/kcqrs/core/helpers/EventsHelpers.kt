package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold

@Suppress("UNCHECKED_CAST")
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

@Suppress("UNCHECKED_CAST")
suspend fun <TAggregate : IAggregate> Flow<IDomainEvent>.foldEvents(initial: TAggregate, version: Long): TAggregate {
    return this.fold(initial) { acc, ev ->
        require(ev.aggregateId == acc.id) { "Each event should be part of the same aggregate. Found ${ev.aggregateId.valueAsString()} in ${acc.id.valueAsString()}" }
        if (version > acc.version) {
            acc.applyEvent(ev) as TAggregate
        } else {
            acc
        }
    }
}

