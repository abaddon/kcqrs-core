package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository
import kotlin.coroutines.CoroutineContext

class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
    repository: IAggregateRepository<TAggregate>,
    coroutineContext: CoroutineContext
) : AggregateCommandHandler<TAggregate>(repository, coroutineContext) {


    override suspend fun onSuccess(updatedAggregate: TAggregate): Result<TAggregate> =
        Result.success(updatedAggregate)


}