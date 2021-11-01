package io.github.abaddon.kcqrs.core

import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import kotlin.reflect.KClass



interface IRouteEvents{
    fun register(klass: KClass<*>, handler: (eventArgs: IEvent) -> IAggregate)

    fun register(aggregate: IAggregate)

    fun dispatch(eventMessage: IEvent): IAggregate
}
