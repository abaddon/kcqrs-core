package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InMemoryEventStoreRepository<TAggregate : IAggregate>(
    private val _streamNameRoot: String,
    private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate
) : EventStoreRepository<TAggregate>() {

    private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()
    private val projectionHandlers = mutableListOf<IProjectionHandler<*>>()

    override val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun aggregateIdStreamName(aggregateId: IIdentity): String =
        "${_streamNameRoot}.${aggregateId.valueAsString()}"

    /**
     * This method should be used only for testing purpose.
     * It allows saving events directly to the Events store without using the aggregate
     */
    fun addEventsToStorage(aggregateId: IIdentity, events: List<IDomainEvent>) {
        persist(aggregateIdStreamName(aggregateId), events, mapOf(), 0)
    }

    /**
     * This method should be used only for testing purpose.
     * It allows getting events directly from the Events store
     */
    fun loadEventsFromStorage(aggregateId: IIdentity): List<IDomainEvent> =
        load(aggregateIdStreamName(aggregateId))

    override fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ) {
        val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
        currentEvents.addAll(uncommittedEvents.toMutableList())
        storage[streamName] = currentEvents
    }

    override fun load(streamName: String, startFrom: Long): List<IDomainEvent> =
        storage.getOrDefault(streamName, listOf())

    override fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        projectionHandlers.add(projectionHandler)
    }

    override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

    override fun publish(events: List<IDomainEvent>) {
        projectionHandlers.forEach { projectionHandlers -> projectionHandlers.onEvents(events) }
    }
}