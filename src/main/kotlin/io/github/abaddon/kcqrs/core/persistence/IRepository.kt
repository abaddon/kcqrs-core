package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface IRepository {

    suspend fun <TAggregate: IAggregate> getById(aggregateId: IIdentity, klass: KClass<TAggregate>):TAggregate

    suspend fun <TAggregate: IAggregate> getById(aggregateId: IIdentity, version: Int, klass: KClass<TAggregate>):TAggregate

    suspend fun save(uncommittedEvents: List<DomainEvent>, commitID: UUID, updateHeaders: Map<String,Objects>)

}