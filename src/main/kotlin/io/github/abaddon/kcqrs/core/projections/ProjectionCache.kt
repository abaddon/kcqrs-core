package io.github.abaddon.kcqrs.core.projections

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe LRU cache for projections with configurable size limit.
 *
 * @param maxSize Maximum number of projections to cache (default: 1000)
 * @param initialCapacity Initial capacity hint for the underlying map
 */
class ProjectionCache<TProjection : IProjection>(
    private val maxSize: Int = 1000,
    initialCapacity: Int = 16
) {
    private val lock = ReentrantReadWriteLock()

    // LinkedHashMap with access-order (LRU)
    private val cache = object : LinkedHashMap<IProjectionKey, TProjection>(
        initialCapacity,
        0.75f,
        true // access-order = true for LRU behavior
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<IProjectionKey, TProjection>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * Retrieves a projection from the cache.
     * @return The cached projection or null if not found
     */
    fun get(key: IProjectionKey): TProjection? = lock.read {
        cache[key]
    }

    /**
     * Stores a projection in the cache.
     * If the cache is at capacity, the least recently used entry is evicted.
     */
    fun put(key: IProjectionKey, projection: TProjection) = lock.write {
        cache[key] = projection
    }

    /**
     * Removes a specific projection from the cache.
     */
    fun remove(key: IProjectionKey): TProjection? = lock.write {
        cache.remove(key)
    }

    /**
     * Clears all cached projections.
     * Should be called when the handler is stopped.
     */
    fun clear() = lock.write {
        cache.clear()
    }

    /**
     * Returns the current number of cached projections.
     */
    fun size(): Int = lock.read {
        cache.size
    }

    /**
     * Returns cache statistics for monitoring.
     */
    fun stats(): CacheStats = lock.read {
        CacheStats(
            currentSize = cache.size,
            maxSize = maxSize
        )
    }
}

/**
 * Simple cache statistics.
 */
data class CacheStats(
    val currentSize: Int,
    val maxSize: Int
) {
    val utilizationPercent: Double = if (maxSize > 0) (currentSize.toDouble() / maxSize) * 100 else 0.0
}
