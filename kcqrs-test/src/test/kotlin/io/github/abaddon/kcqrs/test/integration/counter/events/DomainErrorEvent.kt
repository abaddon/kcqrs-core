package io.github.abaddon.kcqrs.test.integration.counter.events

import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateId
import java.util.*

data class DomainErrorEvent private constructor(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    override val version: Int = 1,
    override val aggregateType: String,
    override val header: EventHeader,
    val errorType: String,
    val errorMsg: String
) : IDomainEvent {

    constructor(aggregateId: CounterAggregateId, error: Exception) : this(
        UUID.randomUUID(),
        aggregateId,
        1,
        "CounterAggregateRoot",
        EventHeader.create("CounterAggregateRoot"),
        error::class.qualifiedName!!,
        error.message.orEmpty()
    )
}