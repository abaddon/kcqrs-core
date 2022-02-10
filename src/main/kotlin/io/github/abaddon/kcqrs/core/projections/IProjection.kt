package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent


interface IProjection {
    val key: IProjectionKey

    fun applyEvent(event: IDomainEvent): IProjection
}