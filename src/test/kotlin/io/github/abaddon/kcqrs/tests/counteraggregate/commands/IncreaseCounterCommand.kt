package io.github.abaddon.kcqrs.tests.counteraggregate.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId

data class IncreaseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
    ): Command(aggregateID) {
}