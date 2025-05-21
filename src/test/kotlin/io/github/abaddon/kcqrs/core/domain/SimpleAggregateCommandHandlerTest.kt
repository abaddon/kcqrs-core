package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryEventStoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class SimpleAggregateCommandHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: InMemoryEventStoreRepository<DummyAggregate>

    private lateinit var dummyAggregateCommandHandler: SimpleAggregateCommandHandler<DummyAggregate>

    @BeforeEach
    fun setup() {
        repository = InMemoryEventStoreRepository(
            "SimpleAggregateCommandHandlerTest",
            { DummyAggregate.empty(it) },
            testDispatcher
        )
        dummyAggregateCommandHandler = SimpleAggregateCommandHandler(repository, testDispatcher);
    }

    @AfterEach
    fun tearDown() {
        repository.cleanup()
    }


    @Test
    fun `Given a command to create an aggregate when the AggregateCommandHandler receive it, then the aggregate is on the repository`() =
        testScope.runTest {
            // Given
            val aggregateId = DummyIdentity(1)
            val cmd1 = NewDummyAggregateCommand(aggregateId)

            //When
            when (dummyAggregateCommandHandler.handle(cmd1)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }

            //Then
            when (val result = repository.getById(aggregateId)) {
                is Result.Valid -> {
                    assertThat(aggregateId).isEqualTo(result.value.id)
                    assertThat(0).isEqualTo(result.value.version)
                }

                is Result.Invalid -> assert(false)
            }

        }

    @Test
    fun `Given a command to update an aggregate when the AggregateCommandHandler receive it, then the aggregate on the repository is updated`() =
        testScope.runTest {
            //Given
            val aggregateId = DummyIdentity(2)
            val cmd1 = NewDummyAggregateCommand(aggregateId)
            val cmd2 = UpdateDummyAggregateCommand(aggregateId)

            //When
            when (dummyAggregateCommandHandler.handle(cmd1)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }
            when (dummyAggregateCommandHandler.handle(cmd2)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }

            //Then
            when (val result = repository.getById(aggregateId)) {
                is Result.Valid -> {
                    assertEquals(aggregateId, result.value.id)
                    assertEquals(1, result.value.version)
                }

                is Result.Invalid -> assert(false)
            }

        }

    @Test
    fun `Given two commands to update an aggregate when the AggregateCommandHandler receive it, then the aggregate on the repository is updated`() =
        testScope.runTest {
            //Given
            val aggregateId = DummyIdentity(3)
            val cmd1 = NewDummyAggregateCommand(aggregateId)
            val cmd2 = UpdateDummyAggregateCommand(aggregateId)
            val cmd3 = UpdateDummyAggregateCommand(aggregateId)

            //When
            when (dummyAggregateCommandHandler.handle(cmd1)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }
            when (dummyAggregateCommandHandler.handle(cmd2)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }
            when (dummyAggregateCommandHandler.handle(cmd3)) {
                is Result.Valid -> assert(true)
                is Result.Invalid -> assert(false)
            }

            //Then
            when (val result = repository.getById(aggregateId)) {
                is Result.Valid -> {
                    assertEquals(aggregateId, result.value.id)
                    assertEquals(2, result.value.version)
                }

                is Result.Invalid -> assert(false)
            }

        }

    private data class NewDummyAggregateCommand(override val aggregateID: DummyIdentity) : ICommand<DummyAggregate> {
        override val messageId: UUID = UUID.randomUUID()
        override fun execute(currentAggregate: DummyAggregate?): DummyAggregate {
            return DummyAggregate.create(aggregateID, 0)
        }

    }

    private data class UpdateDummyAggregateCommand(override val aggregateID: DummyIdentity) : ICommand<DummyAggregate> {
        override val messageId: UUID = UUID.randomUUID()
        override fun execute(currentAggregate: DummyAggregate?): DummyAggregate {
            require(currentAggregate != null)
            return currentAggregate.increaseVersion()
        }

    }

    data class DummyIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String {
            return value.toString()
        }
    };

    private data class DummyEvent(
        override val aggregateId: DummyIdentity
    ) : IDomainEvent {
        override val aggregateType: String = DummyAggregate::class.java.simpleName
        override val version: Int = 1
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

    data class DummyAggregate(
        override val id: DummyIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        fun increaseVersion(): DummyAggregate {
            return raiseEvent(DummyEvent(id)) as DummyAggregate
        }

        private fun apply(event: DummyEvent): DummyAggregate {
            return copy(id = event.aggregateId, version = version + 1)
        }

        companion object {
            fun create(id: DummyIdentity, version: Long): DummyAggregate =
                DummyAggregate(id, version, ArrayList<IDomainEvent>())

            fun empty(id: IIdentity): DummyAggregate =
                when (id) {
                    is DummyIdentity -> DummyAggregate(id, 0, mutableListOf())
                    else -> throw Exception("Unexpected Identity")
                }
        }
    }
}