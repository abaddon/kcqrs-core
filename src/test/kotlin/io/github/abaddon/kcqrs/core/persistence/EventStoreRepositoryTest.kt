package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.InvalidParameterException
import java.util.*

@ExperimentalCoroutinesApi
internal class EventStoreRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: TestEventStoreRepository

    @BeforeEach
    fun setup() {
        repository = TestEventStoreRepository()
    }

    @Test
    fun `Given aggregateId when getting aggregateIdStreamName then returns correct stream name`() {
        val aggregateId = DummyIdentity(123)

        val streamName = repository.aggregateIdStreamName(aggregateId)

        assertEquals("TestStream.123", streamName)
    }

    @Test
    fun `Given non-existent aggregate when getById then returns empty aggregate`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(1)

            val result = repository.getById(aggregateId)

            assertTrue(result.isSuccess)
            assertEquals(0L, result.getOrThrow().version)
            assertEquals(aggregateId, result.getOrThrow().id)
        }

    @Test
    fun `Given aggregate with events when getById then returns hydrated aggregate`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(2)
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 1L))
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 2L))

            val result = repository.getById(aggregateId)

            assertTrue(result.isSuccess)
            assertEquals(2L, result.getOrThrow().version)
            assertEquals(aggregateId, result.getOrThrow().id)
        }

    @Test
    fun `Given version less than or equal to zero when getById with version then returns failure`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(3)

            val result = repository.getById(aggregateId, 0L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidParameterException)
        }

    @Test
    fun `Given aggregate when getById with specific version then returns aggregate at that version`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(4)
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 1L))
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 2L))
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 3L))

            val result = repository.getById(aggregateId, 2L)

            assertTrue(result.isSuccess)
            assertEquals(2L, result.getOrThrow().version)
        }

    @Test
    fun `Given aggregate when getById with version higher than current then returns AggregateVersionException`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(5)
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 1L))
            repository.addEvent(aggregateId, createDummyEvent(aggregateId, 2L))

            val result = repository.getById(aggregateId, 5L)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AggregateVersionException)
        }

    @Test
    fun `Given aggregate with uncommitted events when save then events are persisted`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(6)
            val event1 = createDummyEvent(aggregateId, 1L)
            val event2 = createDummyEvent(aggregateId, 2L)
            val aggregate = DummyAggregate(aggregateId, 2L, mutableListOf(event1, event2))

            val result = repository.save(aggregate, UUID.randomUUID())

            assertTrue(result.isSuccess)
            assertEquals(2, repository.getPersistedEventCount(aggregateId))
        }

    @Test
    fun `Given aggregate when save with custom headers then headers are included`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(7)
            val event = createDummyEvent(aggregateId, 1L)
            val aggregate = DummyAggregate(aggregateId, 1L, mutableListOf(event))
            val customHeaders = mapOf("CustomKey" to "CustomValue")

            val result = repository.save(aggregate, UUID.randomUUID()) { customHeaders }

            assertTrue(result.isSuccess)
            val savedHeaders = repository.getLastHeaders()
            assertTrue(savedHeaders.containsKey("CustomKey"))
            assertEquals("CustomValue", savedHeaders["CustomKey"])
        }

    @Test
    fun `Given aggregate when save then standard headers are added`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(8)
            val event = createDummyEvent(aggregateId, 1L)
            val aggregate = DummyAggregate(aggregateId, 1L, mutableListOf(event))
            val commitId = UUID.randomUUID()

            val result = repository.save(aggregate, commitId)

            assertTrue(result.isSuccess)
            val headers = repository.getLastHeaders()
            assertTrue(headers.containsKey(EventStoreRepository.COMMIT_ID_HEADER))
            assertTrue(headers.containsKey(EventStoreRepository.COMMIT_DATE_HEADER))
            assertTrue(headers.containsKey(EventStoreRepository.AGGREGATE_TYPE_HEADER))
            assertEquals(commitId.toString(), headers[EventStoreRepository.COMMIT_ID_HEADER])
        }

    @Test
    fun `Given aggregate with no uncommitted events when save then no events are persisted`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(9)
            val aggregate = DummyAggregate(aggregateId, 0L, mutableListOf())

            val result = repository.save(aggregate, UUID.randomUUID())

            assertTrue(result.isSuccess)
            assertEquals(0, repository.getPersistedEventCount(aggregateId))
        }

    @Test
    fun `Given aggregate when save then publish is called with events`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(10)
            val event = createDummyEvent(aggregateId, 1L)
            val aggregate = DummyAggregate(aggregateId, 1L, mutableListOf(event))

            val result = repository.save(aggregate, UUID.randomUUID())

            assertTrue(result.isSuccess)
            assertEquals(1, repository.getPublishedEventCount())
        }

    private fun createDummyEvent(aggregateId: DummyIdentity, eventVersion: Long): DummyEvent {
        return DummyEvent(aggregateId, eventVersion)
    }

    private data class DummyIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }

    private data class DummyEvent(
        override val aggregateId: DummyIdentity,
        val eventVersion: Long
    ) : IDomainEvent {
        override val aggregateType: String = "DummyAggregate"
        override val version: Long = eventVersion
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

    private data class DummyAggregate(
        override val id: DummyIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        private fun apply(event: DummyEvent): DummyAggregate {
            return copy(version = version + 1)
        }

        companion object {
            fun empty(id: IIdentity): DummyAggregate =
                when (id) {
                    is DummyIdentity -> DummyAggregate(id, 0, mutableListOf())
                    else -> throw Exception("Unexpected Identity")
                }
        }
    }

    private class TestEventStoreRepository : EventStoreRepository<DummyAggregate>() {
        private val eventStore = mutableMapOf<IIdentity, MutableList<IDomainEvent>>()
        private var lastHeaders: Map<String, String> = emptyMap()
        private var publishedEventCount = 0

        override fun aggregateIdStreamName(aggregateId: IIdentity): String {
            return "TestStream.${aggregateId.valueAsString()}"
        }

        override suspend fun loadEvents(streamName: String, startFrom: Long): Result<Flow<IDomainEvent>> {
            val aggregateId = extractIdFromStreamName(streamName)
            val events = eventStore[aggregateId] ?: emptyList()
            return Result.success(flowOf(*events.toTypedArray()))
        }

        override suspend fun persist(
            streamName: String,
            uncommittedEvents: List<IDomainEvent>,
            header: Map<String, String>,
            currentVersion: Long
        ): Result<Unit> {
            lastHeaders = header
            val aggregateId = extractIdFromStreamName(streamName)
            val events = eventStore.getOrPut(aggregateId) { mutableListOf() }
            events.addAll(uncommittedEvents)
            return Result.success(Unit)
        }

        override suspend fun publish(events: List<IDomainEvent>): Result<Unit> {
            publishedEventCount += events.size
            return super.publish(events)
        }

        override fun emptyAggregate(id: IIdentity): DummyAggregate {
            return DummyAggregate.empty(id)
        }

        fun addEvent(aggregateId: IIdentity, event: IDomainEvent) {
            val events = eventStore.getOrPut(aggregateId) { mutableListOf() }
            events.add(event)
        }

        fun getPersistedEventCount(aggregateId: IIdentity): Int {
            return eventStore[aggregateId]?.size ?: 0
        }

        fun getLastHeaders(): Map<String, String> = lastHeaders

        fun getPublishedEventCount(): Int = publishedEventCount

        private fun extractIdFromStreamName(streamName: String): IIdentity {
            val idValue = streamName.substringAfter(".")
            return DummyIdentity(idValue.toInt())
        }
    }
}
