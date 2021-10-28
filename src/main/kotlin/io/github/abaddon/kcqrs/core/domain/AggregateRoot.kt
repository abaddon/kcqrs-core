package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IRouteEvents
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent

abstract class AggregateRoot(
    private var registeredRoutes: IRouteEvents
) : IAggregate {
    private val uncommittedEvents: MutableCollection<DomainEvent> = ArrayList<DomainEvent>()

    constructor():this(ConventionEventRouter())

    init {
        registeredRoutes.register(this)
    }

    fun register(route : (eventArgs: DomainEvent) -> IAggregate ) {
        registeredRoutes.register(DomainEvent::class, route);
    }

    override fun applyEvent(event: DomainEvent): IAggregate{
        return registeredRoutes.dispatch(event)
        //version.incrementVersion();
        //return this
    }

    override fun uncommittedEvents(): List<DomainEvent> {
        return uncommittedEvents.toList()
    }

    override fun clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    protected fun raiseEvent(event: DomainEvent) {
        //this.applyEvent(event) //TODO serve?
        uncommittedEvents.add(event)
    }

}