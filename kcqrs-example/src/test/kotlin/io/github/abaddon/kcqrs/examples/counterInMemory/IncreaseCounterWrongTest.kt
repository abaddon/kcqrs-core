package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.DomainErrorEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import java.util.*

class IncreaseCounterWrongTest : KcqrsTestSpecification<CounterAggregateRoot>(
        CounterAggregateRoot::class
    ) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2147483647

    override fun onHandler(): IAggregateHandler<CounterAggregateRoot> {
        return CounterAggregateHandler(repository)
    }


    override fun given(): List<DomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId, initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(counterAggregateId, incrementValue)
    }

    override fun expected(): List<DomainEvent> {
        val exception = IllegalStateException("Value 2147483647 not valid, it has to be >= 0 and < 2147483647")
        return listOf(
            DomainErrorEvent(
                counterAggregateId,
                exception
            )
        )
    }

    override fun expectedException(): Exception? {
        return null
    }
}