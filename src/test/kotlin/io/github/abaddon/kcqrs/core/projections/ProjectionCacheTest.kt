package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

internal class ProjectionCacheTest {

    @Test
    fun `Given an empty cache when getting a key then returns null`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)
        val key = DummyProjectionKey("test-key")

        val result = cache.get(key)

        assertNull(result)
    }

    @Test
    fun `Given a cached projection when getting by key then returns the projection`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)
        val key = DummyProjectionKey("test-key")
        val projection = DummyProjection(key, 42)

        cache.put(key, projection)
        val result = cache.get(key)

        assertEquals(projection, result)
        assertEquals(42, result?.eventCount)
    }

    @Test
    fun `Given a cache when adding projections then size is tracked correctly`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)

        assertEquals(0, cache.size())

        cache.put(DummyProjectionKey("key1"), DummyProjection(DummyProjectionKey("key1"), 1))
        assertEquals(1, cache.size())

        cache.put(DummyProjectionKey("key2"), DummyProjection(DummyProjectionKey("key2"), 2))
        assertEquals(2, cache.size())
    }

    @Test
    fun `Given a cache at max capacity when adding new entry then oldest entry is evicted (LRU)`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 3)

        // Fill cache to capacity
        cache.put(DummyProjectionKey("key1"), DummyProjection(DummyProjectionKey("key1"), 1))
        cache.put(DummyProjectionKey("key2"), DummyProjection(DummyProjectionKey("key2"), 2))
        cache.put(DummyProjectionKey("key3"), DummyProjection(DummyProjectionKey("key3"), 3))

        assertEquals(3, cache.size())

        // Access key1 to make it recently used
        cache.get(DummyProjectionKey("key1"))

        // Add new entry - should evict key2 (least recently used)
        cache.put(DummyProjectionKey("key4"), DummyProjection(DummyProjectionKey("key4"), 4))

        assertEquals(3, cache.size())
        assertNotNull(cache.get(DummyProjectionKey("key1"))) // Still present
        assertNull(cache.get(DummyProjectionKey("key2")))    // Evicted
        assertNotNull(cache.get(DummyProjectionKey("key3"))) // Still present
        assertNotNull(cache.get(DummyProjectionKey("key4"))) // Newly added
    }

    @Test
    fun `Given a cached projection when updating then latest value is returned`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)
        val key = DummyProjectionKey("test-key")

        cache.put(key, DummyProjection(key, 1))
        cache.put(key, DummyProjection(key, 2))
        cache.put(key, DummyProjection(key, 3))

        val result = cache.get(key)

        assertEquals(3, result?.eventCount)
        assertEquals(1, cache.size()) // Same key, not 3 entries
    }

    @Test
    fun `Given a cached projection when removed then no longer in cache`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)
        val key = DummyProjectionKey("test-key")
        val projection = DummyProjection(key, 42)

        cache.put(key, projection)
        assertEquals(1, cache.size())

        val removed = cache.remove(key)

        assertEquals(projection, removed)
        assertNull(cache.get(key))
        assertEquals(0, cache.size())
    }

    @Test
    fun `Given a cache with multiple entries when cleared then all entries are removed`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)

        cache.put(DummyProjectionKey("key1"), DummyProjection(DummyProjectionKey("key1"), 1))
        cache.put(DummyProjectionKey("key2"), DummyProjection(DummyProjectionKey("key2"), 2))
        cache.put(DummyProjectionKey("key3"), DummyProjection(DummyProjectionKey("key3"), 3))

        assertEquals(3, cache.size())

        cache.clear()

        assertEquals(0, cache.size())
        assertNull(cache.get(DummyProjectionKey("key1")))
        assertNull(cache.get(DummyProjectionKey("key2")))
        assertNull(cache.get(DummyProjectionKey("key3")))
    }

    @Test
    fun `Given a cache when requesting stats then correct statistics are returned`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 10)

        cache.put(DummyProjectionKey("key1"), DummyProjection(DummyProjectionKey("key1"), 1))
        cache.put(DummyProjectionKey("key2"), DummyProjection(DummyProjectionKey("key2"), 2))

        val stats = cache.stats()

        assertEquals(2, stats.currentSize)
        assertEquals(10, stats.maxSize)
        assertEquals(20.0, stats.utilizationPercent, 0.01)
    }

    @Test
    fun `Given a cache when utilization is at 100 percent then stats reflect that`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 2)

        cache.put(DummyProjectionKey("key1"), DummyProjection(DummyProjectionKey("key1"), 1))
        cache.put(DummyProjectionKey("key2"), DummyProjection(DummyProjectionKey("key2"), 2))

        val stats = cache.stats()

        assertEquals(100.0, stats.utilizationPercent, 0.01)
    }

    @Test
    fun `Given concurrent access when multiple threads read and write then cache remains consistent`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 100)
        val threadCount = 10
        val operationsPerThread = 50
        val latch = CountDownLatch(threadCount)

        val threads = (1..threadCount).map { threadId ->
            thread {
                repeat(operationsPerThread) { i ->
                    // Use modulo to create overlapping keys across threads
                    val keyNum = (threadId * 100 + i) % 150 // 150 total possible keys
                    val key = DummyProjectionKey("key-$keyNum")
                    val projection = DummyProjection(key, i)
                    cache.put(key, projection)
                    cache.get(key)
                }
                latch.countDown()
            }
        }

        latch.await()
        threads.forEach { it.join() }

        // After all operations, cache should not exceed max size
        // Due to LRU eviction, size should be at or below maxSize
        assertTrue(cache.size() <= 100, "Cache size ${cache.size()} exceeds max size 100")
    }

    @Test
    fun `Given LRU cache when accessing entries in specific order then eviction follows LRU policy`() {
        val cache = ProjectionCache<DummyProjection>(maxSize = 3)

        // Fill cache
        val key1 = DummyProjectionKey("key1")
        val key2 = DummyProjectionKey("key2")
        val key3 = DummyProjectionKey("key3")
        val key4 = DummyProjectionKey("key4")

        cache.put(key1, DummyProjection(key1, 1))
        cache.put(key2, DummyProjection(key2, 2))
        cache.put(key3, DummyProjection(key3, 3))

        // Access key1 and key3 (making key2 least recently used)
        cache.get(key1)
        cache.get(key3)

        // Add new entry - should evict key2
        cache.put(key4, DummyProjection(key4, 4))

        assertNotNull(cache.get(key1))
        assertNull(cache.get(key2))    // Evicted (LRU)
        assertNotNull(cache.get(key3))
        assertNotNull(cache.get(key4))
    }

    // Test data classes
    private data class DummyProjectionKey(val name: String) : IProjectionKey {
        override fun key(): String = name
    }

    private data class DummyProjection(
        override val key: DummyProjectionKey,
        val eventCount: Int,
        override val lastUpdated: Instant? = Instant.now(),
        override val lastProcessedEvent: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
    ) : IProjection {
        override fun applyEvent(event: IDomainEvent): DummyProjection {
            return copy(eventCount = eventCount + 1)
        }

        override fun withPosition(event: IDomainEvent): IProjection {
            lastProcessedEvent[event.aggregateType] = event.version
            return this
        }
    }
}
