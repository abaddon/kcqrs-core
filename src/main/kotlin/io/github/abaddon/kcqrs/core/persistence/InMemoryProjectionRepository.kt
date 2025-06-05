package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class InMemoryProjectionRepository<TProjection : IProjection>(
    private val coroutineContext: CoroutineContext,
    private val _emptyProjection: (key: IProjectionKey) -> TProjection
) : IProjectionRepository<TProjection> {

    private val storage: MutableMap<IProjectionKey, TProjection> = mutableMapOf()

    override suspend fun getByKey(key: IProjectionKey): Result<TProjection> = withContext(coroutineContext) {
        runCatching {
            storage.getOrDefault(key, emptyProjection(key))
        }
    }


    override suspend fun save(projection: TProjection, offset: Long): Result<Unit> = withContext(coroutineContext) {
        runCatching {
            storage[projection.key] = projection
        }
    }

    override fun emptyProjection(key: IProjectionKey): TProjection = _emptyProjection(key)

}