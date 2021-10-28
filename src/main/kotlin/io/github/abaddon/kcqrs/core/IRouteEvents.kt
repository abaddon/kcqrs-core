package io.github.abaddon.kcqrs.core

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent


typealias Action<DomainEvent> = (eventArgs: DomainEvent) -> IAggregate

interface IRouteEvents{
    fun register(handler: Action<DomainEvent>)

    fun register(aggregate: IAggregate)

    fun dispatch(eventMessage: DomainEvent): IAggregate
}
