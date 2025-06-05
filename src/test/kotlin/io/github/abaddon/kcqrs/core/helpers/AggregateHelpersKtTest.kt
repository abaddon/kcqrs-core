package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.HandlerForDomainEventNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class AggregateHelpersKtTest {
    @Test
    fun `Given an aggregate when execute throwHandlerNotFound then an exeption is raised`() {
        val dummyAggregateId=DummyAggregateId()
        val event = DummyEvent(dummyAggregateId,1)

        val aggregate= DummyAggregateRoot(dummyAggregateId, 1, 10)

        assertThrows<HandlerForDomainEventNotFoundException>("Aggregate of type DummyAggregateRoot raised an event of type DummyEvent but no handler could be found to handle the message.") {
            aggregate.throwHandlerNotFound(event)
        }
    }

    @Test
    fun `Given an aggregate when apply an event without the event's apply function then a exception is raised`() {
        val dummyAggregateId=DummyAggregateId()
        val event = DummyEvent(dummyAggregateId,1)

        val aggregate= DummyAggregateRoot(dummyAggregateId, 1, 10)

        assertThrows<HandlerForDomainEventNotFoundException>("Aggregate of type DummyAggregateRoot raised an event of type DummyEvent but no handler could be found to handle the message.") {
            aggregate.applyEvent(event)
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

    }
}