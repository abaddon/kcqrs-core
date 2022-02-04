package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import java.util.*

class IncreaseCounterTest: KcqrsTestSpecification<CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2

    override fun onHandler(): IAggregateHandler<CounterAggregateRoot> {
        return CounterAggregateHandler(repository)
    }


    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId,initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(counterAggregateId,incrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterIncreasedEvent(counterAggregateId,incrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}