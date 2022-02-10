package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger


interface IProjectionHandler<TProjection:IProjection> {
    val log: Logger
    val repository: IProjectionRepository<TProjection>

    val projectionKey: IProjectionKey


    @Suppress("UNCHECKED_CAST")
    fun onEvent(event: IDomainEvent) {
        runBlocking {
                val updatedProjection = repository
                    .getByKey(projectionKey)
                    .applyEvent(event) as TProjection
                repository.save(updatedProjection, 0) //TODO offset to define how manage it
        }
    }

    fun onEvents(events: List<IDomainEvent>) {
        events.forEach{ domainEvent ->
            onEvent(domainEvent)
        }
    }

}