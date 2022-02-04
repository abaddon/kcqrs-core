package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.persistence.IRepository
import org.slf4j.Logger

abstract class AggregateHandler<TAggregate: IAggregate> : IAggregateHandler<TAggregate> {
    protected abstract val repository: IRepository<TAggregate>
    protected abstract val logger: Logger

    abstract override suspend fun handle(command: ICommand<TAggregate>)

}