package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class EventsHelpersKtTest {
    @Test
    fun `Given an empty list of events when the list are folded then the final aggregate is the same of the initial one`() {
        val dummyAggregateId = DummyAggregateId()
        val list = listOf<IDomainEvent>()
        val expectedAggregate=DummyAggregateRoot(dummyAggregateId,0,0)

        val initialAggregate=DummyAggregateRoot(dummyAggregateId,0,0)
        val actualAggregate = list.foldEvents(initialAggregate, Long.MAX_VALUE)

        assertEquals(expectedAggregate,actualAggregate,"it should be the same")
    }

    @Test
    fun `Given a list of 1 event when the list are folded then the final aggregate is the merge of all events with the initial aggregate and the version is 1`() {
        val dummyAggregateId = DummyAggregateId()
        val list = listOf<IDomainEvent>(
            DummyEvent(dummyAggregateId,2)
        )
        val expectedAggregate=DummyAggregateRoot(dummyAggregateId,1,2)

        val initialAggregate=DummyAggregateRoot(dummyAggregateId,0,0)
        val actualAggregate = list.foldEvents(initialAggregate, Long.MAX_VALUE)

        assertEquals(expectedAggregate,actualAggregate,"it should be the same")
    }

    @Test
    fun `Given a list of 2 events when the list are folded then the final aggregate is the merge of all events with the initial aggregate and the version is 2`() {
        val dummyAggregateId = DummyAggregateId()
        val list = listOf<IDomainEvent>(
            DummyEvent(dummyAggregateId,2),
            DummyEvent(dummyAggregateId,4)
        )
        val expectedAggregate=DummyAggregateRoot(dummyAggregateId,2,6)

        val initialAggregate=DummyAggregateRoot(dummyAggregateId,0,0)
        val actualAggregate = list.foldEvents(initialAggregate, Long.MAX_VALUE)

        assertEquals(expectedAggregate,actualAggregate,"it should be the same")
    }

    @Test
    fun `Given a list of events with a different AggregateId when the list are folded then an exception is raised`() {
        val dummyAggregateId = DummyAggregateId()
        val list = listOf<IDomainEvent>(
            DummyEvent(DummyAggregateId(),2),
            DummyEvent(DummyAggregateId(),3)
        )
        val initialAggregate=DummyAggregateRoot(dummyAggregateId,0,0)

        assertFailsWith<IllegalArgumentException>{
            list.foldEvents(initialAggregate, Long.MAX_VALUE)
        }
    }

    @Test
    fun `Given a list of events with multiple AggregateIds when the list are folded then the final aggregate contains only the right events`() {
        val dummyAggregateId = DummyAggregateId()
        val list = listOf<IDomainEvent>(
            DummyEvent(DummyAggregateId(),2),
            DummyEvent(DummyAggregateId(),3),
            DummyEvent(dummyAggregateId,10),
            DummyEvent(DummyAggregateId(),10)
        )
        val initialAggregate=DummyAggregateRoot(dummyAggregateId,0,0)

        assertFailsWith<IllegalArgumentException>{
            list.foldEvents(initialAggregate, Long.MAX_VALUE)
        }
    }


    private data class DummyEvent(
        override val messageId: UUID,
        override val aggregateId: DummyAggregateId,
        override val version: Int = 1,
        override val aggregateType: String,
        override val header: EventHeader,
        val value: Int
    ) : IDomainEvent {
        constructor(aggregateId: DummyAggregateId, value: Int) : this(
            UUID.randomUUID(),
            aggregateId,
            1,
            "CounterAggregateRoot",
            EventHeader.create("CounterAggregateRoot"),
            value
        )

    }

    private data class DummyAggregateId(val value: UUID) : IIdentity {
        constructor () : this(UUID.randomUUID())

        override fun valueAsString(): String {
            return value.toString()
        }
    }

    private data class DummyAggregateRoot(
        override val id: DummyAggregateId,
        override val version: Long,
        val counter: Int,
        override val uncommittedEvents: MutableCollection<IDomainEvent> = mutableListOf()
    ) : AggregateRoot() {

        private fun apply(event: DummyEvent): DummyAggregateRoot {
            return copy(id = event.aggregateId, version = version + 1, counter = counter + event.value)
        }
    }
}