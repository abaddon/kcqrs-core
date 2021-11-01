package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import java.util.*

interface IRepository<TAggregate: IAggregate> {

    suspend fun getById(aggregateId: IIdentity):TAggregate

    suspend fun getById(aggregateId: IIdentity, version: Long):TAggregate

    suspend fun save(aggregate: IAggregate, commitID: UUID, updateHeaders: Map<String,String>)

    fun aggregateIdStreamName(aggregateId: IIdentity): String

}