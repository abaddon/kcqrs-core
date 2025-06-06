package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IRouteEvents
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import io.github.abaddon.kcqrs.core.helpers.throwHandlerNotFound
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf


class RegistrationEventRouter: IRouteEvents {


    private val handlers: MutableMap<KType,(eventArgs: IEvent) -> IAggregate> = mutableMapOf()
    private lateinit var registered: IAggregate

    @OptIn(ExperimentalStdlibApi::class)
    override fun register(klass: KClass<*>, handler: (eventArgs: IEvent) -> IAggregate) {
        handlers[typeOf<IDomainEvent>()] = handler
    }

    override fun register(aggregate: IAggregate) {
        registered = aggregate
    }

    override fun dispatch(eventMessage: IEvent): IAggregate {

        val handler = handlers[eventMessage::class.starProjectedType]?.invoke(eventMessage)
        //val handler2: (eventArgs: DomainEvent) -> IAggregate = handlers.get(eventMessage::class.starProjectedType)

        checkNotNull(handler){registered.throwHandlerNotFound(eventMessage)}
        return TODO()
    }
}