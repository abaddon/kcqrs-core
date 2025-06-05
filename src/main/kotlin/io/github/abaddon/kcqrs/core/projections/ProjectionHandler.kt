package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.helpers.flatMap
import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class ProjectionHandler<TProjection : IProjection>(
    override val repository: IProjectionRepository<TProjection>,
    override val projectionKey: IProjectionKey,
    protected val coroutineContext: CoroutineContext
) : IProjectionHandler<TProjection> {

    override suspend fun onEvent(event: IDomainEvent): Result<Unit> = withContext(coroutineContext) {
        onEvents(flowOf(event))
    }

    override suspend fun onEvents(events: List<IDomainEvent>): Result<Unit> = withContext(coroutineContext) {
        onEvents(events.asFlow())
    }

    suspend fun onEvents(events: Flow<IDomainEvent>): Result<Unit> =
        withContext(coroutineContext) {
            repository.getByKey(projectionKey)
                .flatMap {
                    updateProjection(it, events)
                }.flatMap {
                    saveProjection(it, 0)
                }
        }

    @Suppress("UNCHECKED_CAST")
    private suspend fun updateProjection(
        currentProjection: TProjection,
        events: Flow<IDomainEvent>
    ): Result<TProjection> = withContext(coroutineContext) {
        runCatching {
            events.fold(currentProjection) { currentProjection, event ->
                currentProjection.applyEvent(event) as TProjection
            }
        }
    }

    private suspend fun saveProjection(
        projection: TProjection,
        offset: Long = 0
    ): Result<Unit> = withContext(coroutineContext) {
        repository.save(projection, offset)
    }
}