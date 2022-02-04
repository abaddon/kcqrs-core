package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import io.github.abaddon.kcqrs.test.integration.counter.CounterAggregateHandler
import io.github.abaddon.kcqrs.test.integration.counter.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterInitialisedEvent
import java.util.*

class DecreaseCounterTest: KcqrsTestSpecification<CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2
    private val decrementValue = 3

    override fun onHandler(): IAggregateHandler<CounterAggregateRoot> {
        return CounterAggregateHandler(repository)
    }

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId,initialValue),
            CounterIncreasedEvent(counterAggregateId,incrementValue)
        )
    }

    override fun `when`(): ICommand<CounterAggregateRoot> {
        return DecreaseCounterCommand(counterAggregateId,decrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterDecreaseEvent(counterAggregateId,decrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}