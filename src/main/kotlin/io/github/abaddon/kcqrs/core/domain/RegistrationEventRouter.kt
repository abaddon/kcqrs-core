package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.Action
import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IRouteEvents
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.helpers.throwHandlerNotFound
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf


class RegistrationEventRouter: IRouteEvents {


    private val handlers: MutableMap<KType,Action<DomainEvent>> = mutableMapOf()
    private lateinit var regsitered: IAggregate;

    @OptIn(ExperimentalStdlibApi::class)
    override fun register(handler: Action<DomainEvent>) {
        handlers[typeOf<DomainEvent>()] = handler
    }

    override fun register(aggregate: IAggregate) {
        regsitered = aggregate;
    }

    override fun dispatch(eventMessage: DomainEvent): IAggregate {
        val handler: Action<DomainEvent>? = handlers.get(eventMessage::class.starProjectedType)
        checkNotNull(handler){regsitered.throwHandlerNotFound(eventMessage)}
        return handler(eventMessage)
    }
}