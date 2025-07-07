package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import java.util.UUID

interface IAggregateRepository<TAggregate : IAggregate> {

    fun emptyAggregate(aggregateId: IIdentity): TAggregate

    suspend fun getById(aggregateId: IIdentity): Result<TAggregate>

    suspend fun getById(aggregateId: IIdentity, version: Long): Result<TAggregate>

    suspend fun save(aggregate: TAggregate, commitID: UUID, updateHeaders: () -> Map<String, String>): Result<TAggregate>

    suspend fun save(aggregate: TAggregate, commitID: UUID): Result<TAggregate>

}