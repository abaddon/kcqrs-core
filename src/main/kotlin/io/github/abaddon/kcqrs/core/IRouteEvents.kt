package io.github.abaddon.kcqrs.core

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import kotlin.reflect.KClass



interface IRouteEvents{
    fun register(klass: KClass<*>, handler: (eventArgs: DomainEvent) -> IAggregate)

    fun register(aggregate: IAggregate)

    fun dispatch(eventMessage: DomainEvent): IAggregate
}
