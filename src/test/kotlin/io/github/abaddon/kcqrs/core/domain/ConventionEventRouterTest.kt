package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import io.github.abaddon.kcqrs.core.exceptions.HandlerForDomainEventNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class ConventionEventRouterTest {

    @Test
    fun `Given a router with throwOnApplyNotFound true when registering aggregate then apply methods are discovered`() {
        val identity = FakeIdentity(1)
        val aggregate = DummyAggregate.create(identity, 1L)
        val router = ConventionEventRouter(aggregate)

        val event = DummyEvent(identity)
        val result = router.dispatch(event)

        assertEquals(2L, result.version)
        assertEquals(identity, result.id)
    }

    @Test
    fun `Given a router when manually registering event handler then handler is invoked on dispatch`() {
        val identity = FakeIdentity(1)
        val aggregate = DummyAggregate.create(identity, 1L)
        val router = ConventionEventRouter()
        router.register(aggregate)

        val handler: (event: IEvent) -> IAggregate = { e: IEvent ->
            aggregate.copy(version = aggregate.version + 10)
        }
        router.register(DummyEvent::class, handler)

        val event = DummyEvent(identity)
        val result = router.dispatch(event)

        assertEquals(11L, result.version)
    }

    @Test
    fun `Given a router with throwOnApplyNotFound true when dispatching unregistered event then exception is thrown`() {
        val identity = FakeIdentity(1)
        val aggregate = DummyAggregate.create(identity, 1L)
        val router = ConventionEventRouter(true)
        router.register(aggregate)

        val unregisteredEvent = UnregisteredEvent(identity)

        assertThrows<HandlerForDomainEventNotFoundException> {
            router.dispatch(unregisteredEvent)
        }
    }

    @Test
    fun `Given a router with throwOnApplyNotFound false when dispatching unregistered event then original aggregate is returned`() {
        val identity = FakeIdentity(1)
        val aggregate = DummyAggregate.create(identity, 1L)
        val router = ConventionEventRouter(false)
        router.register(aggregate)

        val unregisteredEvent = UnregisteredEvent(identity)
        val result = router.dispatch(unregisteredEvent)

        assertEquals(1L, result.version)
        assertEquals(identity, result.id)
    }

    @Test
    fun `Given a router when registering aggregate with multiple apply methods then all handlers are discovered`() {
        val identity = FakeIdentity(1)
        val aggregate = MultiEventAggregate.create(identity, 0L)
        val router = ConventionEventRouter(aggregate)

        val event1 = DummyEvent(identity)
        val result1 = router.dispatch(event1)
        assertEquals(1L, result1.version)

        val event2 = DummyEvent2(identity)
        val result2 = router.dispatch(event2)
        assertEquals(5L, result2.version)
    }

    private data class FakeIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }

    private data class DummyAggregate(
        override val id: IIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        private fun apply(event: DummyEvent): DummyAggregate {
            return copy(version = version + 1)
        }

        companion object {
            fun create(id: IIdentity, version: Long): DummyAggregate =
                DummyAggregate(id, version, mutableListOf())
        }
    }

    private data class MultiEventAggregate(
        override val id: IIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        private fun apply(event: DummyEvent): MultiEventAggregate {
            return copy(version = version + 1)
        }

        private fun apply(event: DummyEvent2): MultiEventAggregate {
            return copy(version = version + 5)
        }

        companion object {
            fun create(id: IIdentity, version: Long): MultiEventAggregate =
                MultiEventAggregate(id, version, mutableListOf())
        }
    }

    private data class DummyEvent(override val aggregateId: IIdentity) : IDomainEvent {
        override val aggregateType: String = DummyAggregate::class.java.simpleName
        override val version: Long = 1
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

    private data class DummyEvent2(override val aggregateId: IIdentity) : IDomainEvent {
        override val aggregateType: String = MultiEventAggregate::class.java.simpleName
        override val version: Long = 1
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }

    private data class UnregisteredEvent(override val aggregateId: IIdentity) : IDomainEvent {
        override val aggregateType: String = "UnregisteredAggregate"
        override val version: Long = 1
        override val header: EventHeader = EventHeader.create(aggregateType)
        override val messageId: UUID = UUID.randomUUID()
    }
}
