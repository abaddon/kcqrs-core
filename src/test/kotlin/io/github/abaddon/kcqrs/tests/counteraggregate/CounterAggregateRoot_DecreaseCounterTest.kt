package io.github.abaddon.kcqrs.tests.counteraggregate

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.DecreaseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.tests.helpers.KcqrsTestSpecification
import java.util.*

class CounterAggregateRoot_DecreaseCounterTest: KcqrsTestSpecification<DecreaseCounterCommand>() {

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