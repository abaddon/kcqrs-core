package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey

interface IProjectionRepository<TProjection: IProjection> {
    suspend fun getByKey(key: IProjectionKey):TProjection

    suspend fun save(projection: TProjection, offset: Long)

    fun emptyProjection(key: IProjectionKey): TProjection

}