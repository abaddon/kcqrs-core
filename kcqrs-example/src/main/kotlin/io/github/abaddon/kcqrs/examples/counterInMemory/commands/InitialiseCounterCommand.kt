package io.github.abaddon.kcqrs.examples.counterInMemory.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot

data class InitialiseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
    ): Command<CounterAggregateRoot>(aggregateID) {
    override fun execute(currentAggregate: CounterAggregateRoot?): CounterAggregateRoot {
        return CounterAggregateRoot.initialiseCounter(aggregateID, value)
    }
}