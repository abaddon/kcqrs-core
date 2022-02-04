package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand

interface IAggregateHandler<TAggregate: IAggregate> {

    suspend fun handle(command: ICommand<TAggregate>);

}