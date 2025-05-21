package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InMemoryEventStoreRepository<TAggregate : IAggregate>(
    private val _streamNameRoot: String,
    private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventStoreRepository<TAggregate>(dispatcher) {

    private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()
    private val projectionHandlers = mutableListOf<IProjectionHandler<*>>()

    override fun aggregateIdStreamName(aggregateId: IIdentity): String =
        "${_streamNameRoot}.${aggregateId.valueAsString()}"

    override suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ) = withContext(coroutineContext) {
        val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
        currentEvents.addAll(uncommittedEvents.toMutableList())
        storage[streamName] = currentEvents
    }

    override suspend fun load(streamName: String, startFrom: Long): List<IDomainEvent> = withContext(coroutineContext) {
        storage.getOrDefault(streamName, listOf())
    }

    override suspend fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        projectionHandlers.add(projectionHandler)
    }

    override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

    override suspend fun publish(events: List<IDomainEvent>) = withContext(coroutineContext){
        projectionHandlers.forEach { projectionHandlers -> projectionHandlers.onEvents(events) }
    }
}