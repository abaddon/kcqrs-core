package io.github.abaddon.kcqrs.core.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HandlerForDomainEventNotFoundExceptionTest {

    @Test
    fun `Given error message when creating exception then message is stored`() {
        val expectedMessage = "Handler not found for event type: TestEvent"
        val exception = HandlerForDomainEventNotFoundException(expectedMessage)

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `Given null message when creating exception then message is null`() {
        val exception = HandlerForDomainEventNotFoundException(null)

        assertEquals(null, exception.message)
    }

    @Test
    fun `Given exception when checking instance then is Exception subclass`() {
        val exception = HandlerForDomainEventNotFoundException("test message")

        assertTrue(exception is Exception)
    }

    @Test
    fun `Given different messages when creating exceptions then each has its own message`() {
        val message1 = "Handler not found for Event1"
        val message2 = "Handler not found for Event2"

        val exception1 = HandlerForDomainEventNotFoundException(message1)
        val exception2 = HandlerForDomainEventNotFoundException(message2)

        assertEquals(message1, exception1.message)
        assertEquals(message2, exception2.message)
    }
}
