package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import io.github.abaddon.kcqrs.test.sample.counter.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.test.sample.counter.commands.InitialiseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.sample.counter.events.CounterInitialisedEvent
import java.util.*

class CounterAggregateRoot_InitialiseCounterTest: KcqrsTestSpecification<InitialiseCounterCommand, CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val messageId = UUID.randomUUID()
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
        //UUID.randomUUID(),aggregateId, EventHeader(typeOf<DomainErrorEvent>()),1,value
        return listOf(CounterInitialisedEvent(counterAggregateId,initialValue))
    }

    override fun expectedException(): Exception? {
        return null
    }
}