package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf


abstract class DomainEvent<TAggregate: IAggregate> : IEvent{
    abstract val aggregateId: IIdentity
    @OptIn(ExperimentalStdlibApi::class)
    val header: EventHeader = EventHeader(typeOf<IAggregate>().jvmErasure as KClass<TAggregate>)
}