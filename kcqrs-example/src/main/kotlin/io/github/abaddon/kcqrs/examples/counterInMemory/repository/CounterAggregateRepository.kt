package io.github.abaddon.kcqrs.examples.counterInMemory.repository

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.persistence.IRepository
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import java.util.*

class CounterAggregateRepository: IRepository<CounterAggregateRoot> {
    companion object{
        val storage = mutableMapOf<IIdentity,CounterAggregateRoot>()
    }
    override suspend fun getById(aggregateId: IIdentity): CounterAggregateRoot? {
        return storage[aggregateId]
    }

    override suspend fun getById(aggregateId: IIdentity, version: Long): CounterAggregateRoot? {
        return storage[aggregateId]
    }

    override suspend fun save(aggregate: CounterAggregateRoot, commitID: UUID, updateHeaders: Map<String, String>) {
        storage[aggregate.id] = aggregate
    }

    override fun aggregateIdStreamName(aggregateId: IIdentity): String {
        return "stream_${aggregateId.valueAsString()}"
    }
}