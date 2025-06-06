package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.domain.messages.IMessage

interface IEvent: IMessage {
    val version: Long
}