package io.github.abaddon.kcqrs.tests.counteraggregate.events

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateId

data class DomainErrorEvent(
    override val aggregateId: CounterAggregateId,
    val errorType: String,
    val errorMsg: String
) : DomainEvent(aggregateId){

    constructor(aggregateId: CounterAggregateId, e: Exception):this(aggregateId,e.javaClass.simpleName, e.localizedMessage)
}