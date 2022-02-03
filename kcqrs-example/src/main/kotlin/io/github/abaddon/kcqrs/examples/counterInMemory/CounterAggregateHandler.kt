package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.domain.AggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.persistence.IRepository
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class CounterAggregateHandler(override val repository: IRepository<CounterAggregateRoot>): AggregateHandler<CounterAggregateRoot>() {
    override val logger: Logger = LoggerFactory.getLogger(CounterAggregateHandler::class.java.simpleName)

    override suspend fun handle(command: ICommand<CounterAggregateRoot>) {
        val currentAggregate = repository.getById(command.aggregateID)
        val updatedAggregate = command.execute(currentAggregate)
        repository.save(updatedAggregate, UUID.randomUUID(), mapOf())
    }
}