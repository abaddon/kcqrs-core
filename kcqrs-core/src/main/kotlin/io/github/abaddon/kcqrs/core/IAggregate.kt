package io.github.abaddon.kcqrs.core

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent

interface IAggregate{
    val id: IIdentity;
    val version: Long

    fun applyEvent(event: IEvent): IAggregate
    fun uncommittedEvents(): List<DomainEvent<*>>
    fun clearUncommittedEvents()
}