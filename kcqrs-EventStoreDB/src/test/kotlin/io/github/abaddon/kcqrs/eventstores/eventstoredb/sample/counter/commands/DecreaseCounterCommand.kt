package io.github.abaddon.kcqrs.eventstores.eventstoredb.sample.counter.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.eventstores.eventstoredb.sample.counter.entities.CounterAggregateId

data class DecreaseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
    ): Command(aggregateID) {
}