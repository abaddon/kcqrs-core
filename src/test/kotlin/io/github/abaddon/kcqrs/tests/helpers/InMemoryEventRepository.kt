package io.github.abaddon.kcqrs.tests.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.persistence.IRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class InMemoryEventRepository : IRepository {

    val givenEvents: MutableList<DomainEvent> = mutableListOf()
    val events: MutableList<DomainEvent> = mutableListOf()

    fun applyGivenEvents(events: List<DomainEvent>) {
        givenEvents.clear()
        givenEvents.addAll(events)
    }

    override suspend fun <TAggregate : IAggregate> getById(
        aggregateId: IIdentity,
        klass: KClass<TAggregate>
    ): TAggregate {

        return getById(aggregateId, 0, klass)
    }

    override suspend fun <TAggregate : IAggregate> getById(
        aggregateId: IIdentity,
        version: Int,
        klass: KClass<TAggregate>
    ): TAggregate {
        val emptyAggregate = createAggregate(klass)
        return givenEvents.fold2(emptyAggregate)
    }

    private fun <TAggregate : IAggregate> createAggregate(klass: KClass<TAggregate>): TAggregate {
        return klass.createInstance()
    }

    override suspend fun save(
        uncommittedEvents: List<DomainEvent>,
        commitID: UUID,
        updateHeaders: Map<String, Objects>
    ) {
        //events.clear();
        events.addAll(uncommittedEvents)
    }

    fun <TEvent : DomainEvent, TAggregate : IAggregate> Iterable<TEvent>.fold2(initial: TAggregate?): TAggregate {
        var accumulator = initial!!
        for (element in this) {
            accumulator = accumulator.applyEvent(element) as TAggregate
        }
        return accumulator
    }
}