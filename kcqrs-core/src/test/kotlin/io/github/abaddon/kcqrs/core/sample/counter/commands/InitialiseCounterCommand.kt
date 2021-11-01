package io.github.abaddon.kcqrs.core.sample.counter.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.core.sample.counter.entities.CounterAggregateId

data class InitialiseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
    ): Command(aggregateID) {
}