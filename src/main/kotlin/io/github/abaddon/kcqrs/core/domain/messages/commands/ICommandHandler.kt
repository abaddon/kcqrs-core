package io.github.abaddon.kcqrs.core.domain.messages.commands

interface ICommandHandler<in TCommand: ICommand> {

    suspend fun handle(command: TCommand);

}