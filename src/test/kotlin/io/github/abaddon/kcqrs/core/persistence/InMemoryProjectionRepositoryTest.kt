package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class InMemoryProjectionRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    companion object {
        private val  NOW = Instant.now()
    }

    @Test
    fun `given an unavailable projection  when I load it then an empty projection is returned`() = testScope.runTest {
        val projectionRepository = InMemoryProjectionRepository {
            DummyProjection(it as DummyProjectionKey, 0, NOW)
        }

        val key = DummyProjectionKey("key1")

        val actualProjection = projectionRepository.getByKey(key)
        val expectedProjection = DummyProjection(key, 0, NOW)

        assertEquals(expectedProjection, actualProjection.getOrThrow())
    }

    @Test
    fun `given a new projection  when I save it then the projection is persisted`() = testScope.runTest {
        val projectionRepository = InMemoryProjectionRepository {
            DummyProjection(it as DummyProjectionKey, 0, NOW)
        }

        val key = DummyProjectionKey("key1")
        val expectedProjection = DummyProjection(key, 4, NOW)

        projectionRepository.save(expectedProjection)

        val actualProjection = projectionRepository.getByKey(key)


        assertEquals(expectedProjection, actualProjection.getOrThrow())
    }

    data class DummyProjectionKey(val name: String) : IProjectionKey {
        override fun key(): String = name

    }

    data class DummyProjection(
        override val key: DummyProjectionKey, val receivedEvents: Int = 0,
        override val lastUpdated: Instant,
        override val lastProcessedEvent: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
    ) : IProjection {
        override fun applyEvent(event: IDomainEvent): DummyProjection {
            return copy(receivedEvents = receivedEvents + 1)
        }

        override fun withPosition(event: IDomainEvent): IProjection {
            lastProcessedEvent[event.aggregateType] = event.version
            return this
        }
    }
}