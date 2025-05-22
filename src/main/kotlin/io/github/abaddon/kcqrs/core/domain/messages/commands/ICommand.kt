package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.IMessage

interface ICommand<A: IAggregate> : IMessage{
    val aggregateID: IIdentity

    fun execute(currentAggregate: A?): Result<A>
}