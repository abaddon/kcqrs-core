package io.github.abaddon.kcqrs.tests.counteraggregate

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.tests.counteraggregate.commands.IncreaseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.DomainErrorEvent
import io.github.abaddon.kcqrs.tests.helpers.KcqrsTestSpecification
import java.util.*

class CounterAggregateRoot_IncreaseCounterWrongTest: KcqrsTestSpecification<IncreaseCounterCommand>() {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2147483647

    override fun onHandler(): ICommandHandler<IncreaseCounterCommand> {
        return IncreaseCounterCommandCommandHandler(repository)
    }


    override fun given(): List<DomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId,initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(counterAggregateId,incrementValue)
    }

    override fun expected(): List<DomainEvent> {
        return listOf(DomainErrorEvent(counterAggregateId,java.lang.IllegalStateException("Value 2147483647 not valid, it has to be >= 0 and < 2147483647")))
    }

    override fun expectedException(): Exception? {
        return null
    }
}