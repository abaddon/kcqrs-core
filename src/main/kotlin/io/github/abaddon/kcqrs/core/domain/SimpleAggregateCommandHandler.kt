package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository
import java.util.*

open class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
    override val repository: IAggregateRepository<TAggregate>,
) : IAggregateCommandHandler<TAggregate> {
    override suspend fun handle(
        command: ICommand<TAggregate>,
        updateHeaders: () -> Map<String, String>
    ): Result<Exception, TAggregate> =
        when (val actualAggregateResult = repository.getById(command.aggregateID)) {
            is Result.Valid -> {
                try {
                    val newAggregate = command.execute(actualAggregateResult.value)
                    val saveResult=repository.save(newAggregate, UUID.randomUUID(), updateHeaders)
                    when (saveResult) {
                        is Result.Valid -> onSuccess(newAggregate)
                        is Result.Invalid -> onFailure(saveResult.err)
                    }
                    saveResult
                } catch (ex: Exception){
                    Result.Invalid(ex)
                }
            }
            is Result.Invalid -> actualAggregateResult
        }

    open fun onSuccess(updatedAggregate:  TAggregate){}
    open fun onFailure(  err:Exception){}

    override suspend fun handle(command: ICommand<TAggregate>): Result<Exception, TAggregate> =
        handle(command) { mapOf<String, String>() }

}