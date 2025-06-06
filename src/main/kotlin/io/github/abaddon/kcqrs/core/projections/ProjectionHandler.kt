package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.helpers.LoggerFactory.log
import io.github.abaddon.kcqrs.core.helpers.flatMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.cancellation.CancellationException

abstract class ProjectionHandler<TProjection : IProjection>(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : IProjectionHandler<TProjection>, CoroutineScope by scope {

    override suspend fun onEvent(event: IDomainEvent): Result<Unit> = runCatching {
        processEvents(flowOf(event))
    }

    override suspend fun onEvents(events: List<IDomainEvent>): Result<Unit> = runCatching {
        processEvents(events.asFlow())
    }

    override suspend fun onEvents(events: Flow<IDomainEvent>): Result<Unit> = runCatching {
        processEvents(events)
    }

    protected suspend fun processEvents(events: Flow<IDomainEvent>): Result<Unit> {
        return repository.getByKey(projectionKey)
            .flatMap { currentProjection ->
                val filteredEvents = filterProcessedEvents(currentProjection, events)
                updateProjection(currentProjection, filteredEvents)
            }.flatMap {
                saveProjection(it)
            }.onFailure { exception ->
                stop(exception)
            }
    }

    private fun filterProcessedEvents(currentProjection: TProjection, events: Flow<IDomainEvent>): Flow<IDomainEvent> =
        events.filter { event ->
            event.version > currentProjection.lastProcessedPositionOf(event.aggregateType)
        }

    @Suppress("UNCHECKED_CAST")
    private suspend fun updateProjection(
        currentProjection: TProjection,
        events: Flow<IDomainEvent>
    ): Result<TProjection> = runCatching {
        events.fold(currentProjection) { currentProjection, event ->
            try {
                val updated = applyEventToProjection(currentProjection, event)
                @Suppress("UNCHECKED_CAST")
                updated.withPosition(event) as TProjection
            } catch (e: Exception) {
                handleEventError(currentProjection, event, e)
            }
        }
    }

    protected open suspend fun applyEventToProjection(
        projection: TProjection,
        event: IDomainEvent
    ): TProjection {
        @Suppress("UNCHECKED_CAST")
        return projection.applyEvent(event) as TProjection
    }

    protected open suspend fun handleEventError(
        projection: TProjection,
        event: IDomainEvent,
        error: Exception
    ): TProjection {
        log.error(
            "Failed to apply event ${event.messageId} to projection ${projectionKey.key()}",
            error
        )
        // Default: skip the event
        // Override for custom error handling (e.g., dead letter queue)
        return projection
    }

    private suspend fun saveProjection(
        projection: TProjection
    ): Result<Unit> =
        repository.save(projection)

    fun start() {
        log.info("Starting projection handler for ${projectionKey.key()}")
    }

    fun stop(exception: Throwable?) {
        if (isActive) {
            val message = "Stopping projection handler for ${projectionKey.key()}"
            log.error(message)
            cancel(CancellationException(message, exception?.cause))
        }
    }
}