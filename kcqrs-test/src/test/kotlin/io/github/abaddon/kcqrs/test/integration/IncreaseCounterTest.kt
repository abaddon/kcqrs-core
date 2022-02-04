package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import io.github.abaddon.kcqrs.test.integration.counter.CounterAggregateHandler
import io.github.abaddon.kcqrs.test.integration.counter.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterInitialisedEvent
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