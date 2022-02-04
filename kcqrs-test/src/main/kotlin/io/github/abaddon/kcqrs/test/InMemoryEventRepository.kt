package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import io.github.abaddon.kcqrs.core.persistence.IRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class InMemoryEventRepository<TAggregate:IAggregate>(private val klass: KClass<TAggregate>) : IRepository<TAggregate> {

    val givenEvents: MutableList<IDomainEvent> = mutableListOf()
    val events: MutableList<IDomainEvent> = mutableListOf()

    fun applyGivenEvents(events: List<IDomainEvent>) {
        givenEvents.clear()
        givenEvents.addAll(events)
    }
    override fun aggregateIdStreamName(aggregateId: IIdentity): String {
        TODO("Not yet implemented")
    }
    override suspend fun getById(
        aggregateId: IIdentity
    ): TAggregate {
        return getById(aggregateId, 0)
    }

    override suspend fun getById(
        aggregateId: IIdentity,
        version: Long
    ): TAggregate {
        val emptyAggregate: TAggregate = createAggregate(klass)
        return givenEvents.foldEvents(emptyAggregate)
    }

    private fun <TAggregate : IAggregate> createAggregate(klass: KClass<TAggregate>): TAggregate {
        return klass.createInstance()
    }

    override suspend fun save(
        aggregate: TAggregate,
        commitID: UUID,
        updateHeaders: Map<String, String>
    ) {
        events.clear();
        events.addAll(aggregate.uncommittedEvents())
    }


}