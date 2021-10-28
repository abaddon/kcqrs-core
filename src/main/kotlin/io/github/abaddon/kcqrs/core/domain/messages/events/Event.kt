package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.IIdentity
import java.util.*

open class Event(
    override val messageId: UUID,
    open val aggregateId: IIdentity,
    val header: EventHeader,
    override val version: Int
) : IEvent {

    constructor(
        aggregateId: IIdentity,
        correlationId: UUID,
        eventType: String = Event::class.java.simpleName,
        who: String = "anonymous"
    ) : this(UUID.randomUUID(), aggregateId, EventHeader(who, eventType,correlationId),0)

    constructor(
        aggregateId: IIdentity,
        eventType: String = Event::class.java.simpleName,
        who: String = "anonymous"
    ) : this(UUID.randomUUID(), aggregateId, EventHeader(who, eventType,UUID.randomUUID()),0)
}