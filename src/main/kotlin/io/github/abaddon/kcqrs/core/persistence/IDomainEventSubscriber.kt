package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler

interface IDomainEventSubscriber<TProjection : IProjection> {

    fun subscribe(projectionHandler: IProjectionHandler<TProjection>)

}