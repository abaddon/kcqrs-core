package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.persistence.IRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class CommandHandler<TCommand: ICommand, TAggregate: IAggregate>(
    protected val repository: IRepository<TAggregate>,
    protected val logger: Logger
): ICommandHandler<TCommand> {

    constructor(repository: IRepository<TAggregate>):this(repository,LoggerFactory.getLogger(CommandHandler::class.java.simpleName))

    abstract override suspend fun handle(command: TCommand)

}