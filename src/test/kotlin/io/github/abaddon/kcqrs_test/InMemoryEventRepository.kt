package io.github.abaddon.kcqrs_test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.persistence.IRepository
import java.util.*
import kotlin.reflect.KClass

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
        val emptyAggregate = createAggregate(klass::class.java)
        return givenEvents.fold2(emptyAggregate)
    }

    fun <TAggregate : IAggregate> createAggregate(clazz: Class<out KClass<TAggregate>>): TAggregate? {
        return clazz.constructors.find { it.parameters.isEmpty() }?.newInstance() as TAggregate
    }

    override suspend fun save(
        uncommittedEvents: List<DomainEvent>,
        commitID: UUID,
        updateHeaders: Map<String, Objects>
    ) {
        events.clear();
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