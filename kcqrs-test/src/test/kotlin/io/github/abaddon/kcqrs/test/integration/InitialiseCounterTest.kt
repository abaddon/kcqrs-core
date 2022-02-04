package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import io.github.abaddon.kcqrs.test.integration.counter.CounterAggregateHandler
import io.github.abaddon.kcqrs.test.integration.counter.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.integration.counter.events.CounterInitialisedEvent
import java.util.*

class InitialiseCounterTest: KcqrsTestSpecification<CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val messageId = UUID.randomUUID()
    private val initialValue = 5

    override fun onHandler(): IAggregateHandler<CounterAggregateRoot> {
        return CounterAggregateHandler(repository)
    }


    override fun given(): List<IDomainEvent> {
        return listOf()
    }

    override fun `when`(): InitialiseCounterCommand {
        return InitialiseCounterCommand(counterAggregateId,initialValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterInitialisedEvent(counterAggregateId,initialValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}