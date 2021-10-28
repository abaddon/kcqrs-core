package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.IMessage

interface ICommand : IMessage{
    val aggregateID: IIdentity
}