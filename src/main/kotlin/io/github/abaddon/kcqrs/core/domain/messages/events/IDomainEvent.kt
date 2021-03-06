package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.IIdentity

interface IDomainEvent : IEvent {
    val aggregateId: IIdentity
    val aggregateType: String
    val header: EventHeader
}
