package io.github.abaddon.kcqrs.tests.counteraggregate.entities

import io.github.abaddon.kcqrs.core.IIdentity
import java.util.*

data class CounterAggregateId(private val value: UUID) : IIdentity {

    override fun value(): UUID {
        return value
    }

    companion object{
        fun create(): CounterAggregateId {
            return CounterAggregateId(UUID.randomUUID())
        }
    }
}