package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey

class InMemoryProjectionRepository<TProjection: IProjection>(
    private val _emptyProjection: (key: IProjectionKey) -> TProjection
): IProjectionRepository<TProjection> {

    private val storage: MutableMap<IProjectionKey,TProjection> = mutableMapOf()

    override suspend fun getByKey(key: IProjectionKey): TProjection =
        storage.getOrDefault(key,emptyProjection(key))


    override suspend fun save(projection: TProjection, offset: Long) {
        storage[projection.key] = projection
    }

    override fun emptyProjection(key: IProjectionKey): TProjection = _emptyProjection(key)

}