package io.github.abaddon.kcqrs.test.sample.counter.events

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.sample.counter.entities.CounterAggregateRoot
import java.util.*

data class DomainErrorEvent(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    override val version: Int = 1,
    val errorType: String,
    val errorMsg: String
) : DomainEvent<CounterAggregateRoot>(){

    @OptIn(ExperimentalStdlibApi::class)
    constructor(aggregateId: CounterAggregateId, error: Exception):this(UUID.randomUUID(),aggregateId,1,error::class.qualifiedName!!,error.message.orEmpty())
}