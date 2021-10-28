package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.persistence.IRepository
import org.slf4j.LoggerFactory
import org.slf4j.Logger

abstract class CommandHandler<TCommand: ICommand>(
    protected val repository: IRepository,
    protected val logger: Logger
): ICommandHandler<TCommand> {

    constructor(repository: IRepository):this(repository,LoggerFactory.getLogger(CommandHandler::class.java.simpleName))

    abstract override suspend fun handle(command: TCommand)

}