package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryProjectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
internal class SimpleProjectionHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: InMemoryProjectionRepository<DummyProjection>
    private val projectionKey = DummyProjectionKey("simple-handler-key")

    @BeforeEach
    fun setup() {
        repository = InMemoryProjectionRepository { key ->
            DummyProjection(key as DummyProjectionKey, 0)
        }
    }

    @Test
    fun `Given a SimpleProjectionHandler when created then projection key is set`() =
        testScope.runTest {
            val handler = SimpleProjectionHandler(repository, projectionKey)
            val event = createDummyEvent(1, 1L)

            val actualKey = handler.getProjectionKey(event)

            assertEquals(projectionKey, actualKey)
        }

    @Test
    fun `Given a SimpleProjectionHandler when processing event then always uses same projection key`() =
        testScope.runTest {
            val handler = SimpleProjectionHandler(repository, projectionKey)
            val event1 = createDummyEvent(1, 1L)
            val event2 = createDummyEvent(2, 2L)

            val key1 = handler.getProjectionKey(event1)
            val key2 = handler.getProjectionKey(event2)

            assertEquals(projectionKey, key1)
            assertEquals(projectionKey, key2)
            assertEquals(key1, key2)
        }

    @Test
    fun `Given a SimpleProjectionHandler when processing events then all events update same projection`() =
        testScope.runTest {
            val handler = SimpleProjectionHandler(repository, projectionKey)
            val events = listOf(
                createDummyEvent(1, 1L),
                createDummyEvent(2, 2L),
                createDummyEvent(3, 3L)
            )

            handler.onEvents(events)

            val projection = repository.getByKey(projectionKey).getOrThrow()
            assertEquals(3, projection.eventCount)
        }

    @Test
    fun `Given a SimpleProjectionHandler when onEvent is called then projection is updated correctly`() =
        testScope.runTest {
            val handler = SimpleProjectionHandler(repository, projectionKey)
            val event = createDummyEvent(1, 5L)

            val result = handler.onEvent(event)

            assertTrue(result.isSuccess)
            val projection = repository.getByKey(projectionKey).getOrThrow()
            assertEquals(1, projection.eventCount)
            assertEquals(5L, projection.lastProcessedEvent["TestAggregate"])
        }

    @Test
    fun `Given a SimpleProjectionHandler when onEvents with flow is called then all events are processed`() =
        testScope.runTest {
            val handler = SimpleProjectionHandler(repository, projectionKey)
            val events = flowOf(
                createDummyEvent(1, 1L),
                createDummyEvent(2, 2L)
            )

            val result = handler.onEvents(events)

            assertTrue(result.isSuccess)
            val projection = repository.getByKey(projectionKey).getOrThrow()
            assertEquals(2, projection.eventCount)
        }

    @Test
    fun `Given multiple SimpleProjectionHandler instances with different keys when processing events then projections are separate`() =
        testScope.runTest {
            val key1 = DummyProjectionKey("projection-1")
            val key2 = DummyProjectionKey("projection-2")
            val handler1 = SimpleProjectionHandler(repository, key1)
            val handler2 = SimpleProjectionHandler(repository, key2)

            handler1.onEvent(createDummyEvent(1, 1L))
            handler2.onEvents(listOf(createDummyEvent(2, 1L), createDummyEvent(3, 2L)))

            val projection1 = repository.getByKey(key1).getOrThrow()
            val projection2 = repository.getByKey(key2).getOrThrow()

            assertEquals(1, projection1.eventCount)
            assertEquals(2, projection2.eventCount)
        }

    private fun createDummyEvent(id: Int, version: Long, aggregateType: String = "TestAggregate"): DummyEvent {
        return DummyEvent(
            DummyIdentity(id),
            version,
            aggregateType
        )
    }

    private data class DummyIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }

    private data class DummyEvent(
        override val aggregateId: DummyIdentity,
        override val version: Long,
        override val aggregateType: String
    ) : IDomainEvent {
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

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
