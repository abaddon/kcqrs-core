package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*
import kotlin.coroutines.CoroutineContext

open class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
    override val repository: IAggregateRepository<TAggregate>,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAggregateCommandHandler<TAggregate> {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = job + dispatcher

    override suspend fun handle(
        command: ICommand<TAggregate>,
        updateHeaders: () -> Map<String, String>
    ): Result<TAggregate> {
        return repository.getById(command.aggregateID)
            .onSuccess { aggregate ->
                try {
                    val newAggregate = command.execute(aggregate)
                    repository.save(newAggregate, UUID.randomUUID(), updateHeaders)
                        .onSuccess {
                            onSuccess(newAggregate)
                            Result.success(newAggregate)
                        }
                } catch (ex: Exception) {
                    Result.failure(ex)
                }
            }
            .onFailure {
                return Result.failure(it)
            }
    }

//    when (
//    val actualAggregateResult = repository.getById(command.aggregateID))
//    {
//        is actualAggregateResult.is -> {
//        try {
//            val newAggregate = command.execute(actualAggregateResult.value)
//            repository.save(newAggregate, UUID.randomUUID(), updateHeaders)
//            onSuccess(newAggregate)
//            Result.Valid(newAggregate)
//        } catch (ex: Exception) {
//            Result.Invalid(ex)
//        }
//    }
//
//        is Result.Invalid -> actualAggregateResult


    open fun onSuccess(updatedAggregate: TAggregate) {}
    open fun onFailure(err: Exception) {}

    override suspend fun handle(command: ICommand<TAggregate>): Result<TAggregate> =
        handle(command) { mapOf<String, String>() }

}