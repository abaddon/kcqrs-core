package io.github.abaddon.kcqrs.tests.counteraggregate

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.InitialiseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.tests.helpers.KcqrsTestSpecification
import java.util.*

class CounterAggregateRoot_InitialiseCounterTest: KcqrsTestSpecification<InitialiseCounterCommand>() {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5

    override fun onHandler(): ICommandHandler<InitialiseCounterCommand> {
        return InitialiseCounterCommandCommandHandler(repository)
    }


    override fun given(): List<DomainEvent> {
        return listOf()
    }

    override fun `when`(): InitialiseCounterCommand {
        return InitialiseCounterCommand(counterAggregateId,initialValue)
    }

    override fun expected(): List<DomainEvent> {
        return listOf(CounterInitialisedEvent(counterAggregateId,initialValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}