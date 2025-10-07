package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class CommandTest {

    @Test
    fun `Given aggregateID and who when creating command then command is initialized with correct values`() {
        val aggregateId = FakeIdentity(1)
        val who = "test-user"

        val command = TestCommand(aggregateId, who)

        assertEquals(aggregateId, command.aggregateID)
        assertEquals(who, command.commandHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
        assertNotNull(command.messageId)
        assertNotNull(command.commandHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID))
        assertNotNull(command.commitDate)
    }

    @Test
    fun `Given aggregateID only when creating command then who defaults to anonymous`() {
        val aggregateId = FakeIdentity(2)

        val command = TestCommand(aggregateId)

        assertEquals(aggregateId, command.aggregateID)
        assertEquals("anonymous", command.commandHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
    }

    @Test
    fun `Given two commands created when comparing messageIds then they are different`() {
        val aggregateId = FakeIdentity(3)

        val command1 = TestCommand(aggregateId, "user1")
        val command2 = TestCommand(aggregateId, "user1")

        assertNotEquals(command1.messageId, command2.messageId)
    }

    @Test
    fun `Given two commands created when comparing correlationIds then they are different`() {
        val aggregateId = FakeIdentity(4)

        val command1 = TestCommand(aggregateId, "user1")
        val command2 = TestCommand(aggregateId, "user1")

        assertNotEquals(
            command1.commandHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID),
            command2.commandHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID)
        )
    }

    @Test
    fun `Given a command when execute is called then result contains aggregate`() {
        val aggregateId = FakeIdentity(5)
        val command = TestCommand(aggregateId, "test-user")

        val result = command.execute(null)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(aggregateId, result.getOrNull()?.id)
    }

    @Test
    fun `Given a command with existing aggregate when execute is called then aggregate is updated`() {
        val aggregateId = FakeIdentity(6)
        val existingAggregate = DummyAggregate.create(aggregateId, 5L)
        val command = UpdateCommand(aggregateId, "test-user")

        val result = command.execute(existingAggregate)

        assertTrue(result.isSuccess)
        assertEquals(6L, result.getOrNull()?.version)
    }

    @Test
    fun `Given command headers when accessing properties then correct values are returned`() {
        val who = "test-user"
        val correlationId = UUID.randomUUID()
        val headers = CommandHeaders(who, correlationId)

        assertEquals(who, headers.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
        assertEquals(correlationId.toString(), headers.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID))
    }

    private data class FakeIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }

    private class TestCommand(aggregateID: IIdentity, who: String = "anonymous") :
        Command<DummyAggregate>(aggregateID, who) {

        override fun execute(currentAggregate: DummyAggregate?): Result<DummyAggregate> {
            return Result.success(DummyAggregate.create(aggregateID, 0L))
        }
    }

    private class UpdateCommand(aggregateID: IIdentity, who: String = "anonymous") :
        Command<DummyAggregate>(aggregateID, who) {

        override fun execute(currentAggregate: DummyAggregate?): Result<DummyAggregate> {
            requireNotNull(currentAggregate) { "Aggregate must exist for update" }
            return Result.success(currentAggregate.copy(version = currentAggregate.version + 1))
        }
    }

    private data class DummyAggregate(
        override val id: IIdentity,
        override val version: Long,
        override val uncommittedEvents: MutableCollection<IDomainEvent>
    ) : AggregateRoot() {

        companion object {
            fun create(id: IIdentity, version: Long): DummyAggregate =
                DummyAggregate(id, version, mutableListOf())
        }
    }
}
