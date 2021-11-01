package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

internal class AggregateRootTest {

    @Test
    fun `Given an identity when create the aggregate then the aggregate has the same identity`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = FakeAggregate(identity, version)

        assertEquals(identity, aggregate.id)
        assertEquals(version, aggregate.version)

    }

    @Test
    fun `Given an event registered when apply the event then the aggregate is updated`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = FakeAggregate(identity, version)

        val expectedVersion = 2L

        val handler: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent1(e as FakeEvent) }
        aggregate.register(FakeEvent::class, handler);

        val updatedAggregate = aggregate.applyEvent(FakeEvent(identity))
        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)

    }

    @Test
    fun `Given 2 events registered when apply 2 events then the aggregate is updated and there are not uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = FakeAggregate(identity, version)

        val expectedVersion = 7L

        val handlerEvent1: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent1(e as FakeEvent) }
        aggregate.register(FakeEvent::class, handlerEvent1);

        val handlerEvent2: (event: IEvent) -> IAggregate = { e: IEvent -> aggregate.executeEvent2(e as FakeEvent2) }
        aggregate.register(FakeEvent2::class, handlerEvent2);

        val event1 = FakeEvent(identity)
        val event2 = FakeEvent2(identity)

        val updatedAggregate = aggregate
            .applyEvent(event1)
            .applyEvent(event2)

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(0,updatedAggregate.uncommittedEvents().size)
        updatedAggregate.clearUncommittedEvents()
        assertEquals(0,updatedAggregate.uncommittedEvents().size)

    }

    @Test
    fun `Given an aggregate when call generateFakeEvent1() then the aggregate is updated and there is one uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = FakeAggregate(identity, version)

        val expectedVersion = 2L

        val updatedAggregate = aggregate.generateFakeEvent1()

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(1,updatedAggregate.uncommittedEvents().size)
        updatedAggregate.clearUncommittedEvents()
        assertEquals(0,updatedAggregate.uncommittedEvents().size)
    }

    @Test
    fun `Given an aggregate with FakeEvent applied when call generateFakeEvent2() then the aggregate is updated and there are two uncommittedEvents`() {
        val identity = FakeIdentity(1);
        val version = 1L;
        val aggregate = FakeAggregate(identity, version)

        val expectedVersion = 7L

        val updatedAggregate = aggregate
            .generateFakeEvent1()
            .generateFakeEvent2()

        assertEquals(identity, updatedAggregate.id)
        assertEquals(expectedVersion, updatedAggregate.version)
        assertEquals(2,updatedAggregate.uncommittedEvents().size)
    }


}
data class FakeIdentity(val value: Int) : IIdentity {
    override fun valueAsString(): String {
        return value.toString()
    }
};

data class FakeAggregate private constructor(
    override val id: IIdentity,
    override val version: Long,
    override val uncommittedEvents: MutableCollection<DomainEvent<*>>
) : AggregateRoot() {

    constructor(id: IIdentity,version: Long): this(id, version,ArrayList<DomainEvent<*>>())

    fun generateFakeEvent1(): FakeAggregate{
        return raiseEvent(FakeEvent(id)) as FakeAggregate
    }

    fun generateFakeEvent2(): FakeAggregate{
        return raiseEvent(FakeEvent2(id)) as FakeAggregate
    }

    private fun apply(event: FakeEvent): FakeAggregate {
        return copy(id = event.aggregateId, version = version + 1)
    }

    private fun apply(event: FakeEvent2): FakeAggregate {
        return copy(id = event.aggregateId, version = version + 5)
    }

    fun executeEvent1(event: FakeEvent): FakeAggregate {
        return copy(id = event.aggregateId, version = version + 1)
    }

    fun executeEvent2(event: FakeEvent2): FakeAggregate {
        return copy(id = event.aggregateId, version = version + 5)
    }
}

data class FakeEvent(
    override val aggregateId: IIdentity
): DomainEvent<FakeAggregate>() {
    override val version: Int = 1
    override val messageId: UUID = UUID.randomUUID()
}

data class FakeEvent2(
    override val aggregateId: IIdentity
): DomainEvent<FakeAggregate>() {
    override val version: Int = 1
    override val messageId: UUID = UUID.randomUUID()
}
