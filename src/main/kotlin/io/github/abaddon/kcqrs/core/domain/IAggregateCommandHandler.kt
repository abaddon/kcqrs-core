package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.persistence.IAggregateRepository

interface IAggregateCommandHandler<TAggregate: IAggregate> {
    val repository: IAggregateRepository<TAggregate>

    suspend fun handle(command: ICommand<TAggregate>)

    suspend fun handle(command: ICommand<TAggregate>, updateHeaders: () -> Map<String,String>)

}