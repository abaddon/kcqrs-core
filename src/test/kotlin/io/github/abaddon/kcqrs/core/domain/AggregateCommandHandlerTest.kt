package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryEventStoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@ExperimentalCoroutinesApi
internal class AggregateCommandHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: InMemoryEventStoreRepository<DummyAggregate>
    private lateinit var handler: TestAggregateCommandHandler

    @BeforeEach
    fun setup() {
        repository = InMemoryEventStoreRepository(
            "AggregateCommandHandlerTest",
            { DummyAggregate.empty(it) }
        )
        handler = TestAggregateCommandHandler(repository)
    }

    @Test
    fun `Given a command when handle is called then onSuccess is invoked`() = testScope.runTest {
        val aggregateId = DummyIdentity(1)
        val command = CreateDummyCommand(aggregateId)

        val result = handler.handle(command)

        assertTrue(result.isSuccess)
        assertTrue(handler.onSuccessCalled)
        assertEquals(1, handler.onSuccessCallCount)
    }

    @Test
    fun `Given a command when handle is called with custom headers then headers are passed to repository`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(2)
            val command = CreateDummyCommand(aggregateId)
            val customHeaders = mapOf("CustomHeader" to "CustomValue")

            val result = handler.handle(command) { customHeaders }

            assertTrue(result.isSuccess)
            assertTrue(handler.onSuccessCalled)
        }

    @Test
    fun `Given a failing command when handle is called then error is returned and onSuccess is not called`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(3)
            val command = FailingCommand(aggregateId)

            val result = handler.handle(command)

            assertTrue(result.isFailure)
            assertFalse(handler.onSuccessCalled)
            assertEquals(0, handler.onSuccessCallCount)
        }

    @Test
    fun `Given a command that updates aggregate when handle is called then aggregate is persisted`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(4)
            handler.handle(CreateDummyCommand(aggregateId))

            val updateCommand = UpdateDummyCommand(aggregateId)
            val result = handler.handle(updateCommand)

            assertTrue(result.isSuccess)
            val aggregate = repository.getById(aggregateId).getOrThrow()
            assertEquals(1L, aggregate.version)
        }

    @Test
    fun `Given multiple commands when handle is called sequentially then onSuccess is called for each`() =
        testScope.runTest {
            val id1 = DummyIdentity(5)
            val id2 = DummyIdentity(6)

            handler.handle(CreateDummyCommand(id1))
            handler.handle(CreateDummyCommand(id2))

            assertEquals(2, handler.onSuccessCallCount)
        }

    @Test
    fun `Given a command when handle is called then aggregate is loaded from repository`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(7)
            // Pre-create aggregate
            handler.handle(CreateDummyCommand(aggregateId))

            // Reset counter
            handler.onSuccessCallCount = 0

            // Update it
            val result = handler.handle(UpdateDummyCommand(aggregateId))

            assertTrue(result.isSuccess)
            val aggregate = result.getOrThrow()
            assertEquals(1L, aggregate.version)
        }

    @Test
    fun `Given a command that generates events when handle is called then events are persisted`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(8)
            val command = CreateDummyCommand(aggregateId)

            handler.handle(command)

            val aggregate = repository.getById(aggregateId).getOrThrow()
            assertNotNull(aggregate)
            assertEquals(0L, aggregate.version)
        }

    @Test
    fun `Given onSuccess returns failure when handle is called then failure is propagated`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(9)
            val command = CreateDummyCommand(aggregateId)
            handler.shouldFailOnSuccess = true

            val result = handler.handle(command)

            assertTrue(result.isFailure)
            assertTrue(handler.onSuccessCalled)
        }

    @Test
    fun `Given a command when handle is called without custom headers then default headers are used`() =
        testScope.runTest {
            val aggregateId = DummyIdentity(10)
            val command = CreateDummyCommand(aggregateId)

            val result = handler.handle(command)

            assertTrue(result.isSuccess)
        }

    private data class DummyIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }

    private data class CreateDummyCommand(override val aggregateID: DummyIdentity) : ICommand<DummyAggregate> {
        override val messageId: UUID = UUID.randomUUID()
        override fun execute(currentAggregate: DummyAggregate?): Result<DummyAggregate> {
            return Result.success(DummyAggregate.create(aggregateID, 0L))
        }
    }

    private data class UpdateDummyCommand(override val aggregateID: DummyIdentity) : ICommand<DummyAggregate> {
        override val messageId: UUID = UUID.randomUUID()
        override fun execute(currentAggregate: DummyAggregate?): Result<DummyAggregate> {
            requireNotNull(currentAggregate)
            return Result.success(currentAggregate.increaseVersion())
        }
    }

    private data class FailingCommand(override val aggregateID: DummyIdentity) : ICommand<DummyAggregate> {
        override val messageId: UUID = UUID.randomUUID()
        override fun execute(currentAggregate: DummyAggregate?): Result<DummyAggregate> {
            return Result.failure(RuntimeException("Command execution failed"))
        }
    }

    private data class DummyEvent(override val aggregateId: DummyIdentity) : IDomainEvent {
        override val aggregateType: String = "DummyAggregate"
        override val version: Long = 1
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

    private data class DummyAggregate(
        override val id: DummyIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        fun increaseVersion(): DummyAggregate {
            return raiseEvent(DummyEvent(id)) as DummyAggregate
        }

        private fun apply(event: DummyEvent): DummyAggregate {
            return copy(version = version + 1)
        }

        companion object {
            fun create(id: DummyIdentity, version: Long): DummyAggregate =
                DummyAggregate(id, version, mutableListOf())

            fun empty(id: IIdentity): DummyAggregate =
                when (id) {
                    is DummyIdentity -> DummyAggregate(id, 0, mutableListOf())
                    else -> throw Exception("Unexpected Identity")
                }
        }
    }

    private class TestAggregateCommandHandler(
        repository: InMemoryEventStoreRepository<DummyAggregate>
    ) : AggregateCommandHandler<DummyAggregate>(repository) {

        var onSuccessCalled = false
        var onSuccessCallCount = 0
        var shouldFailOnSuccess = false

        override suspend fun onSuccess(updatedAggregate: DummyAggregate): Result<DummyAggregate> {
            onSuccessCalled = true
            onSuccessCallCount++
            return if (shouldFailOnSuccess) {
                Result.failure(RuntimeException("onSuccess failed"))
            } else {
                Result.success(updatedAggregate)
            }
        }
    }
}
