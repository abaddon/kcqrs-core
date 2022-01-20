package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.IIdentity

interface DomainEvent : IEvent {
    val aggregateId: IIdentity
    val aggregateType: String
    val header: EventHeader

//    fun serialise(item : T): String
//    fun deserialize(json: String): T
}
