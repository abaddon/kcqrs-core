package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository

class SimpleProjectionHandler<TProjection : IProjection>(
    override val repository: IProjectionRepository<TProjection>,
    private val projectionKey: IProjectionKey
) : ProjectionHandler<TProjection>() {
    override fun getProjectionKey(event: IDomainEvent): IProjectionKey {
        return projectionKey
    }
}