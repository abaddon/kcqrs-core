package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import io.github.abaddon.kcqrs.test.sample.counter.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.test.sample.counter.commands.DecreaseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.sample.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.sample.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.sample.counter.events.CounterInitialisedEvent
import java.util.*

class CounterAggregateRoot_DecreaseCounterTest: KcqrsTestSpecification<DecreaseCounterCommand, CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2
    private val decrementValue = 3

    override fun onHandler(): ICommandHandler<DecreaseCounterCommand> {
        return DecreaseCounterCommandCommandHandler(repository)
    }

    override fun given(): List<DomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId,initialValue),
            CounterIncreasedEvent(counterAggregateId,incrementValue)
        )
    }

    override fun `when`(): DecreaseCounterCommand {
        return DecreaseCounterCommand(counterAggregateId,decrementValue)
    }

    override fun expected(): List<DomainEvent> {
        return listOf(CounterDecreaseEvent(counterAggregateId,decrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}