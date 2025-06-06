package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
import kotlinx.coroutines.flow.Flow


interface IProjectionHandler<TProjection : IProjection> {
    val repository: IProjectionRepository<TProjection>
    val projectionKey: IProjectionKey


    suspend fun onEvent(event: IDomainEvent): Result<Unit>

    suspend fun onEvents(events: List<IDomainEvent>): Result<Unit>

    suspend fun onEvents(events: Flow<IDomainEvent>): Result<Unit>

}