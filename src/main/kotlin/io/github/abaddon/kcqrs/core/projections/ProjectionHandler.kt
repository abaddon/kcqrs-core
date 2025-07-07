package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.helpers.LoggerFactory.log
import io.github.abaddon.kcqrs.core.helpers.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.isActive
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

    protected suspend fun processEvents(events: Flow<IDomainEvent>) {
        events.collect { event ->
            //TODO cache the projection to avoid retrieve it every time
            repository.getByKey(getProjectionKey(event))
                .flatMap { currentProjection ->
                    val filteredEvent = filterProcessedEvent(currentProjection, event)
                    if (filteredEvent == null) {
                        log.debug("Skipping already processed event ${event.messageId} for projection ${currentProjection.key.key()}")
                        return@collect
                    }
                    updateProjection(currentProjection, filteredEvent)
                }.flatMap {
                    saveProjection(it)
                }.onFailure { exception ->
                    stop(exception)
                }
        }

    }

    private fun filterProcessedEvents(currentProjection: TProjection, events: Flow<IDomainEvent>): Flow<IDomainEvent> =
        events.filter { event ->
            event.version > currentProjection.lastProcessedPositionOf(event.aggregateType)
        }

    private fun filterProcessedEvent(currentProjection: TProjection, event: IDomainEvent): IDomainEvent? =
        if (event.version > currentProjection.lastProcessedPositionOf(event.aggregateType)) {
            event
        } else {
            log.debug("Skipping already processed event ${event.messageId} for projection ${currentProjection.key.key()}")
            null
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

    @Suppress("UNCHECKED_CAST")
    private suspend fun updateProjection(
        currentProjection: TProjection,
        event: IDomainEvent
    ): Result<TProjection> = runCatching {
        try {
            val updated = applyEventToProjection(currentProjection, event)
            @Suppress("UNCHECKED_CAST")
            updated.withPosition(event) as TProjection
        } catch (e: Exception) {
            handleEventError(currentProjection, event, e)
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
            "Failed to apply event ${event.messageId} to projection ${projection.key}",
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

    fun stop(exception: Throwable?) {
        if (isActive) {
            val message = "Stopping projection handler"
            log.error(message)
            cancel(CancellationException(message, exception?.cause))
        }
    }
}