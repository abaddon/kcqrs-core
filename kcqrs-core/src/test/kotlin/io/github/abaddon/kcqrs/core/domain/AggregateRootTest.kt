package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class AggregateRootTest {

    @Test
    fun `Given an identity when create the aggregate then the aggregate has the same identity`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = DummyAggregate.create(identity, version)

        assertEquals(identity, aggregate.id)
        assertEquals(version, aggregate.version)

    }

    @Test
    fun `Given an event registered when apply the event then the aggregate is updated`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = DummyAggregate.create(identity, version)

        val expectedVersion = 2L

        val handler: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent1(e as DummyEvent) }
        aggregate.register(DummyEvent::class, handler);

        val updatedAggregate = aggregate.applyEvent(DummyEvent(identity))
        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)

    }

    @Test
    fun `Given 2 events registered when apply 2 events then the aggregate is updated and there are not uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = DummyAggregate.create(identity, version)

        val expectedVersion = 7L

        val handlerEvent1: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent1(e as DummyEvent) }
        aggregate.register(DummyEvent::class, handlerEvent1);

        val handlerEvent2: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent2(e as DummyEvent2) }
        aggregate.register(DummyEvent2::class, handlerEvent2);

        val event1 = DummyEvent(identity)
        val event2 = DummyEvent2(identity)

        val updatedAggregate = aggregate
            .applyEvent(event1)
            .applyEvent(event2)

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(0, updatedAggregate.uncommittedEvents().size)
        updatedAggregate.clearUncommittedEvents()
        assertEquals(0, updatedAggregate.uncommittedEvents().size)

    }

    @Test
    fun `Given an aggregate when call generateFakeEvent1() then the aggregate is updated and there is one uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = DummyAggregate.create(identity, version)

        val expectedVersion = 2L

        val updatedAggregate = aggregate.generateFakeEvent1()

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(1, updatedAggregate.uncommittedEvents().size)
        updatedAggregate.clearUncommittedEvents()
        assertEquals(0, updatedAggregate.uncommittedEvents().size)
    }

    @Test
    fun `Given an aggregate with FakeEvent applied when call generateFakeEvent2() then the aggregate is updated and there are two uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = DummyAggregate.create(identity, version)

        val expectedVersion = 7L

        val updatedAggregate = aggregate
            .generateFakeEvent1()
            .generateFakeEvent2()

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(2, updatedAggregate.uncommittedEvents().size)
    }


}

data class FakeIdentity(val value: Int) : IIdentity {
    override fun valueAsString(): String {
        return value.toString()
    }
};

data class DummyAggregate private constructor(
    override val id: IIdentity,
    override val version: Long,
    override val uncommittedEvents: MutableCollection<DomainEvent>
) : AggregateRoot() {

    fun generateFakeEvent1(): DummyAggregate {
        return raiseEvent(DummyEvent(id)) as DummyAggregate
    }

    fun generateFakeEvent2(): DummyAggregate {
        return raiseEvent(DummyEvent2(id)) as DummyAggregate
    }

    private fun apply(event: DummyEvent): DummyAggregate {
        return copy(id = event.aggregateId, version = version + 1)
    }

    private fun apply(event: DummyEvent2): DummyAggregate {
        return copy(id = event.aggregateId, version = version + 5)
    }

    fun executeEvent1(event: DummyEvent): DummyAggregate {
        return copy(id = event.aggregateId, version = version + 1)
    }

    fun executeEvent2(event: DummyEvent2): DummyAggregate {
        return copy(id = event.aggregateId, version = version + 5)
    }

    companion object {
        fun create(id: IIdentity, version: Long): DummyAggregate = DummyAggregate(id, version, ArrayList<DomainEvent>())
    }
}

data class DummyEvent(
    override val aggregateId: IIdentity
) : DomainEvent {
    override val aggregateType: String = DummyAggregate.javaClass.simpleName
    override val version: Int = 1
    override val header: EventHeader = EventHeader.create("DummyAggregate")
    override val messageId: UUID = UUID.randomUUID()
}

data class DummyEvent2(
    override val aggregateId: IIdentity
) : DomainEvent {
    override val aggregateType: String = DummyAggregate.javaClass.simpleName
    override val version: Int = 1
    override val header: EventHeader = EventHeader.create("DummyAggregate")
    override val messageId: UUID = UUID.randomUUID()
}
