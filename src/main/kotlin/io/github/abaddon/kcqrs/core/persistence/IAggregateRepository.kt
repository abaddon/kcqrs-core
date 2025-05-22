package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import java.util.*

interface IAggregateRepository<TAggregate : IAggregate> {

    suspend fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>)

    fun emptyAggregate(aggregateId: IIdentity): TAggregate

    suspend fun getById(aggregateId: IIdentity): Result<TAggregate>

    suspend fun getById(aggregateId: IIdentity, version: Long): Result<TAggregate>

    suspend fun save(aggregate: TAggregate, commitID: UUID, updateHeaders: () -> Map<String, String>): Result<TAggregate>

    suspend fun save(aggregate: TAggregate, commitID: UUID): Result<TAggregate>

}