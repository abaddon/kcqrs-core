package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class InMemoryEventStoreRepository<TAggregate : IAggregate>(
    private val _streamNameRoot: String,
    private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate
) : EventStoreRepository<TAggregate>() {

    private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()
    private val projectionHandlers = mutableListOf<IProjectionHandler<*>>()

    override fun aggregateIdStreamName(aggregateId: IIdentity): String =
        "${_streamNameRoot}.${aggregateId.valueAsString()}"

    override suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit> = runCatching {
        val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
        currentEvents.addAll(uncommittedEvents.toMutableList())
        storage[streamName] = currentEvents
    }

    override suspend fun loadEvents(streamName: String, startFrom: Long): Result<Flow<IDomainEvent>> =
        runCatching {
            storage.getOrDefault(streamName, listOf()).asFlow()
        }

    override suspend fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        projectionHandlers.add(projectionHandler)
    }

    override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

    override suspend fun publish(events: List<IDomainEvent>): Result<Unit> =
        runCatching {
            projectionHandlers.forEach { projectionHandlers -> projectionHandlers.onEvents(events) }
        }

}