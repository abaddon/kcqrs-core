package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleProjectionHandler<TProjection : IProjection>(
    override val repository: IProjectionRepository<TProjection>,
    override val projectionKey: IProjectionKey
) : IProjectionHandler<TProjection> {
    override val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
}

//updateHeaders: () -> Map<String,String>