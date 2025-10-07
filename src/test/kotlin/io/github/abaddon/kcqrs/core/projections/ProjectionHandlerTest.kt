package io.github.abaddon.kcqrs.core.projections

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.IProjectionRepository
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
internal class ProjectionHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: InMemoryProjectionRepository<DummyProjection>

    @BeforeEach
    fun setup() {
        repository = InMemoryProjectionRepository { key ->
            DummyProjection(key as DummyProjectionKey, 0)
        }
    }

    @Test
    fun `Given a projection handler when onEvent is called then projection is updated and saved`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val event = createDummyEvent(1, 1L)

            val result = handler.onEvent(event)

            assertTrue(result.isSuccess)
            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(1, projection.eventCount)
            assertEquals(1L, projection.lastProcessedEvent["TestAggregate"])
        }

    @Test
    fun `Given a projection handler when onEvents list is called then all events are processed`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val events = listOf(
                createDummyEvent(1, 1L),
                createDummyEvent(2, 2L),
                createDummyEvent(3, 3L)
            )

            val result = handler.onEvents(events)

            assertTrue(result.isSuccess)
            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(3, projection.eventCount)
            assertEquals(3L, projection.lastProcessedEvent["TestAggregate"])
        }

    @Test
    fun `Given a projection handler when onEvents flow is called then all events are processed`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val events = flowOf(
                createDummyEvent(1, 1L),
                createDummyEvent(2, 2L)
            )

            val result = handler.onEvents(events)

            assertTrue(result.isSuccess)
            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(2, projection.eventCount)
            assertEquals(2L, projection.lastProcessedEvent["TestAggregate"])
        }

    @Test
    fun `Given a projection handler when duplicate event is sent then it is skipped`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val event1 = createDummyEvent(1, 1L)
            val event2 = createDummyEvent(2, 1L) // Same version

            handler.onEvent(event1)
            handler.onEvent(event2)

            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(1, projection.eventCount) // Only first event processed
        }

    @Test
    fun `Given a projection handler when events arrive out of order then only newer events are processed`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val event1 = createDummyEvent(1, 3L)
            val event2 = createDummyEvent(2, 2L) // Older version
            val event3 = createDummyEvent(3, 4L) // Newer version

            handler.onEvent(event1)
            handler.onEvent(event2)
            handler.onEvent(event3)

            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(2, projection.eventCount) // Only event1 and event3
            assertEquals(4L, projection.lastProcessedEvent["TestAggregate"])
        }

    @Test
    fun `Given a projection handler when event application throws exception then error is handled`() =
        testScope.runTest {
            val handler = ErrorThrowingProjectionHandler(repository)
            val event = createDummyEvent(1, 1L)

            val result = handler.onEvent(event)

            // Handler should handle error gracefully
            assertTrue(result.isSuccess)
            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            // Event should be skipped due to error
            assertEquals(0, projection.eventCount)
        }

    @Test
    fun `Given a projection handler when multiple aggregate types send events then positions are tracked separately`() =
        testScope.runTest {
            val handler = TestProjectionHandler(repository)
            val event1 = createDummyEvent(1, 1L, "AggregateType1")
            val event2 = createDummyEvent(2, 1L, "AggregateType2")
            val event3 = createDummyEvent(3, 2L, "AggregateType1")

            handler.onEvents(listOf(event1, event2, event3))

            val projection = repository.getByKey(DummyProjectionKey("test-key")).getOrThrow()
            assertEquals(3, projection.eventCount)
            assertEquals(2L, projection.lastProcessedEvent["AggregateType1"])
            assertEquals(1L, projection.lastProcessedEvent["AggregateType2"])
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

    private class TestProjectionHandler(
        override val repository: IProjectionRepository<DummyProjection>
    ) : ProjectionHandler<DummyProjection>() {

        override fun getProjectionKey(event: IDomainEvent): IProjectionKey {
            return DummyProjectionKey("test-key")
        }
    }

    private class ErrorThrowingProjectionHandler(
        override val repository: IProjectionRepository<DummyProjection>
    ) : ProjectionHandler<DummyProjection>() {

        override fun getProjectionKey(event: IDomainEvent): IProjectionKey {
            return DummyProjectionKey("test-key")
        }

        override suspend fun applyEventToProjection(
            projection: DummyProjection,
            event: IDomainEvent
        ): DummyProjection {
            throw RuntimeException("Simulated error")
        }
    }
}
