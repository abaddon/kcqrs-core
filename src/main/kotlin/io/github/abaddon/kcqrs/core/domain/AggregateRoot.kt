package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IRouteEvents
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import kotlin.reflect.KClass

abstract class AggregateRoot(
    private var registeredRoutes: IRouteEvents
) : IAggregate {
    abstract val uncommittedEvents: MutableCollection<IDomainEvent>

    constructor():this(ConventionEventRouter())

    init {
        registeredRoutes.register(this)
    }

    fun <T:IEvent>register(kClass: KClass<T>,route : (eventArgs: IEvent) -> IAggregate ) {
        registeredRoutes.register(kClass, route)
    }

    override fun applyEvent(event: IEvent): IAggregate{
        return registeredRoutes.dispatch(event)
    }

    override fun uncommittedEvents(): List<IDomainEvent> {
        return uncommittedEvents.toList()
    }

    override fun clearUncommittedEvents()  {
        uncommittedEvents.clear()
    }

    protected fun raiseEvent(event: IDomainEvent): AggregateRoot {
        val updatedAggregate: AggregateRoot = applyEvent(event) as AggregateRoot
        updatedAggregate.uncommittedEvents.add(event)
        return updatedAggregate
    }

}