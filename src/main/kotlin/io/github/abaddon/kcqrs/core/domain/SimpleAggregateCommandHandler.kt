package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository
import java.util.*

class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
    override val repository: IAggregateRepository<TAggregate>,
) : IAggregateCommandHandler<TAggregate> {
    override suspend fun handle(
        command: ICommand<TAggregate>,
        updateHeaders: () -> Map<String, String>
    ): Result<Exception, TAggregate> =
        when (val actualAggregateResult = repository.getById(command.aggregateID)) {
            is Result.Valid -> {
                val newAggregate = command.execute(actualAggregateResult.value)
                repository.save(newAggregate, UUID.randomUUID(), updateHeaders)
            }
            is Result.Invalid -> actualAggregateResult
        }

    override suspend fun handle(command: ICommand<TAggregate>): Result<Exception, TAggregate> =
        handle(command) { mapOf<String, String>() }
}