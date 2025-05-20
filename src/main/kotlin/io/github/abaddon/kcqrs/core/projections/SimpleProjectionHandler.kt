package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository

class SimpleProjectionHandler<TProjection : IProjection>(
    override val repository: IProjectionRepository<TProjection>,
    override val projectionKey: IProjectionKey
) : IProjectionHandler<TProjection>