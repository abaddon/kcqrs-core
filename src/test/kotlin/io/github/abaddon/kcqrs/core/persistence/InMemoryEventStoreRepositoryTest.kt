package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.Result
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.core.projections.SimpleProjectionHandler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class InMemoryEventStoreRepositoryTest{
    private  val repository = InMemoryEventStoreRepository<DummyAggregate>("InMemoryEventStoreRepositoryTest"){
        DummyAggregate.empty(it)
    }
    @Test
    fun `given an aggregateId when get the aggregateIdStream then the streamId contain the aggregateId`(){
        val  identity= DummyIdentity(1)
        val actualStreamName = repository.aggregateIdStreamName(identity)
        val expectedStreamName = "InMemoryEventStoreRepositoryTest.${identity.valueAsString()}"
        assertEquals(expectedStreamName,actualStreamName)
    }

    @Test
    fun `given an aggregate with 2 events uncommitted when saved then the events are in the repository`() = runBlocking {

        val  identity= DummyIdentity(1)
        val uncommittedEvents= listOf(DummyEvent(identity),DummyEvent(identity))
        val aggregate= DummyAggregate(identity,0, uncommittedEvents.toMutableList())

        repository.save(aggregate, UUID.randomUUID())

        val actualEventsStored=repository.loadEventsFromStorage(identity)

        assertEquals(uncommittedEvents,actualEventsStored)
    }

    @Test
    fun `given an aggregate with 0 events uncommitted when saved then the no events are in the repository`() = runBlocking {

        val  identity= DummyIdentity(1)
        val uncommittedEvents= listOf<IDomainEvent>()
        val aggregate= DummyAggregate(identity,0, uncommittedEvents.toMutableList())

        repository.save(aggregate, UUID.randomUUID())

        val actualEventsStored=repository.loadEventsFromStorage(identity)

        assertEquals(uncommittedEvents,actualEventsStored)
    }

    @Test
    fun `given an aggregate with 1 events uncommitted and 2 committed when saved then the events in the repository are 3`() = runBlocking {

        val  identity= DummyIdentity(1)
        //Persist the first 2 events
        val committedEvents= listOf(DummyEvent(identity),DummyEvent(identity))
        val aggregate= DummyAggregate(identity,0, committedEvents.toMutableList())
        repository.save(aggregate, UUID.randomUUID())

        val uncommittedEvents= listOf(DummyEvent(identity))
        val expectedEvents = committedEvents.plus(uncommittedEvents)

        //Persist the last event
        when(val resultGet= repository.getById(identity)){
            is Result.Invalid -> assert(false)
            is Result.Valid -> {
                val aggregateLoaded=resultGet.value
                val uncommittedAggregate = aggregateLoaded.copy(uncommittedEvents = uncommittedEvents.toMutableList())
                repository.save(uncommittedAggregate, UUID.randomUUID())
            }
        }

        val actualEventsStored=repository.loadEventsFromStorage(identity)

        assertEquals(expectedEvents,actualEventsStored)
    }

    @Test
    fun `given 2 events when add them then 2 events are in the repository`() = runBlocking {

        val  identity= DummyIdentity(1)
        val eventsToAdd= listOf<IDomainEvent>()

        repository.addEventsToStorage(identity,eventsToAdd)

        val actualEventsStored=repository.loadEventsFromStorage(identity)

        assertEquals(eventsToAdd,actualEventsStored)
    }

    @Test
    fun `given a projectionHandler when an event is persisted then it's published`() = runBlocking {

        val repositoryProjection = InMemoryProjectionRepository<DummyProjection>(){
            DummyProjection(it as DummyProjectionKey,0)
        }
        val projectionKey= DummyProjectionKey("projection1")
        val projectionHandle = SimpleProjectionHandler<DummyProjection>(repositoryProjection,projectionKey)

        val repositoryWithSub = InMemoryEventStoreRepository<DummyAggregate>("InMemoryEventStoreRepositoryTest"){
            DummyAggregate.empty(it)
        }
        repositoryWithSub.subscribe(projectionHandle)

        val  identity= DummyIdentity(1)
        val uncommittedEvents= listOf<IDomainEvent>(DummyEvent(identity))
        val aggregate= DummyAggregate(identity,0, uncommittedEvents.toMutableList())

        repositoryWithSub.save(aggregate, UUID.randomUUID())

        val actualProjection = repositoryProjection.getByKey(projectionKey)
        val expectedProjection = DummyProjection(projectionKey,1)

        assertEquals(expectedProjection,actualProjection)
    }


    data class DummyProjectionKey(val name: String ): IProjectionKey{
        override fun key(): String = name

    }
    data class DummyProjection(override val key: DummyProjectionKey,val receivedEvents: Int =0): IProjection{
        override fun applyEvent(event: IDomainEvent): DummyProjection {
            return copy(receivedEvents = receivedEvents+1)
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