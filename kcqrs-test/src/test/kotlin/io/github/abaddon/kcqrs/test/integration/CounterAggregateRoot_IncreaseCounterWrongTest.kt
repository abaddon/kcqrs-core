package io.github.abaddon.kcqrs.test.integration

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.test.sample.counter.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.test.sample.counter.commands.IncreaseCounterCommandCommandHandler
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.sample.counter.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.sample.counter.events.DomainErrorEvent
import io.github.abaddon.kcqrs.test.KcqrsTestSpecification
import java.lang.IllegalStateException
import java.util.*
import kotlin.reflect.typeOf

class CounterAggregateRoot_IncreaseCounterWrongTest: KcqrsTestSpecification<IncreaseCounterCommand, CounterAggregateRoot>(
    CounterAggregateRoot::class) {

    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2147483647

    override fun onHandler(): ICommandHandler<IncreaseCounterCommand> {
        return IncreaseCounterCommandCommandHandler(repository)
    }


    override fun given(): List<DomainEvent<*>> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId,initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(counterAggregateId,incrementValue)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun expected(): List<DomainEvent<*>> {
        return listOf(DomainErrorEvent(UUID.randomUUID(),counterAggregateId,1, IllegalStateException::class.qualifiedName!!,"Value 2147483647 not valid, it has to be >= 0 and < 2147483647"))
    }

    override fun expectedException(): Exception? {
        return null
    }
}