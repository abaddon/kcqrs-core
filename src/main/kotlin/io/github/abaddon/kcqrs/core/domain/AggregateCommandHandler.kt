package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.helpers.LoggerFactory.log
import io.github.abaddon.kcqrs.core.helpers.flatMap
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class AggregateCommandHandler<TAggregate : IAggregate>(
    override val repository: IAggregateRepository<TAggregate>,
    protected val coroutineContext: CoroutineContext
) : IAggregateCommandHandler<TAggregate> {

    override suspend fun handle(command: ICommand<TAggregate>): Result<TAggregate> =
        handle(command) { mapOf<String, String>() }

    override suspend fun handle(
        command: ICommand<TAggregate>,
        updateHeaders: () -> Map<String, String>
    ): Result<TAggregate> = withContext(coroutineContext) {
        log.debug("command ${command::class.java.simpleName} execution started for aggregate ${command.aggregateID.valueAsString()}")
        repository.getById(command.aggregateID)
            .flatMap { existingAggregate ->
                log.debug("Aggregate ${existingAggregate.id.valueAsString()} loaded from the repository")
                command.execute(existingAggregate)
            }
            .flatMap { updatedAggregate ->
                log.debug("Aggregate ${updatedAggregate.id.valueAsString()} updated by command ${command::class.java.simpleName}")
                repository.save(updatedAggregate, UUID.randomUUID(), updateHeaders)
            }.flatMap { persistedAggregate ->
                log.debug("Aggregate ${persistedAggregate.id.valueAsString()} saved to the repository")
                onSuccess(persistedAggregate)
            }
    }

    abstract suspend fun onSuccess(updatedAggregate: TAggregate): Result<TAggregate>

}