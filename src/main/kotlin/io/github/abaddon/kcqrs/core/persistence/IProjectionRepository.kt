package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey

interface IProjectionRepository<TProjection: IProjection> {
    suspend fun getByKey(key: IProjectionKey):Result<TProjection>

    suspend fun save(projection: TProjection):Result<Unit>

    fun emptyProjection(key: IProjectionKey): TProjection

}