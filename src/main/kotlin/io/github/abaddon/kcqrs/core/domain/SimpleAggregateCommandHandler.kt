package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository

class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
    repository: IAggregateRepository<TAggregate>
) : AggregateCommandHandler<TAggregate>(repository) {


    override suspend fun onSuccess(updatedAggregate: TAggregate): Result<TAggregate> =
        Result.success(updatedAggregate)


}