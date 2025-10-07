package io.github.abaddon.kcqrs.core.exceptions

import io.github.abaddon.kcqrs.core.IIdentity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AggregateVersionExceptionTest {

    @Test
    fun `Given version mismatch parameters when creating exception then message contains all details`() {
        val aggregateId = FakeIdentity(123)
        val className = "TestAggregate"
        val aggregateVersion = 5L
        val requestedVersion = 3L

        val exception = AggregateVersionException(
            aggregateId,
            className,
            aggregateVersion,
            requestedVersion
        )

        val message = exception.message
        assertTrue(message!!.contains(requestedVersion.toString()))
        assertTrue(message.contains(aggregateId.valueAsString()))
        assertTrue(message.contains(className))
        assertTrue(message.contains(aggregateVersion.toString()))
    }

    @Test
    fun `Given exception when getting message then format is correct`() {
        val aggregateId = FakeIdentity(456)
        val className = "OrderAggregate"
        val aggregateVersion = 10L
        val requestedVersion = 7L

        val exception = AggregateVersionException(
            aggregateId,
            className,
            aggregateVersion,
            requestedVersion
        )

        val expectedMessage = "Requested version $requestedVersion of aggregate $aggregateId (type $className) - aggregate version is $aggregateVersion"
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `Given different aggregate versions when creating exception then each has unique message`() {
        val aggregateId1 = FakeIdentity(1)
        val aggregateId2 = FakeIdentity(2)

        val exception1 = AggregateVersionException(aggregateId1, "Class1", 5L, 3L)
        val exception2 = AggregateVersionException(aggregateId2, "Class2", 10L, 8L)

        assertTrue(exception1.message!!.contains("1"))
        assertTrue(exception1.message!!.contains("Class1"))
        assertTrue(exception2.message!!.contains("2"))
        assertTrue(exception2.message!!.contains("Class2"))
    }

    @Test
    fun `Given exception when thrown then is instance of Exception`() {
        val exception = AggregateVersionException(
            FakeIdentity(1),
            "TestClass",
            5L,
            3L
        )

        assertTrue(exception is Exception)
    }

    private data class FakeIdentity(val value: Int) : IIdentity {
        override fun valueAsString(): String = value.toString()
    }
}
