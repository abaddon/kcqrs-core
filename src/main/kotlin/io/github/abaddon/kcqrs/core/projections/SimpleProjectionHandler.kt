package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
import kotlin.coroutines.CoroutineContext

class SimpleProjectionHandler<TProjection : IProjection>(
    override val repository: IProjectionRepository<TProjection>,
    override val projectionKey: IProjectionKey,
    coroutineContext: CoroutineContext
) : ProjectionHandler<TProjection>(repository, projectionKey, coroutineContext)