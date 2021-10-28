package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.IIdentity
import java.util.*


abstract class DomainEvent : Event,IDomainEvent {

    constructor(aggregateId: IIdentity,
                correlationId: UUID,
                eventType: String = DomainEvent::class.java.simpleName,
                who: String = "anonymous"):super(aggregateId, correlationId, eventType, who)

    constructor(aggregateId: IIdentity,
                eventType: String = DomainEvent::class.java.simpleName,
                who: String = "anonymous"):super(aggregateId, eventType, who)
}