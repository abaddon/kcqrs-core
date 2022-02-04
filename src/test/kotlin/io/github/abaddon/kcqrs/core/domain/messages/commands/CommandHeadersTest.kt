package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

internal class CommandHeadersTest{

    @Test
    fun `Given who and a correlation Id When I create a new CommandHeaders then the CommandHeaders has this information`(){
        val who ="I don't know"
        val correlationID = UUID.randomUUID()
        val commandHeaders = CommandHeaders(who,correlationID)

        assertEquals("",commandHeaders.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(commandHeaders.standardValue(HeadersType.WHEN))
        assertEquals(who,commandHeaders.standardValue(HeadersType.WHO))
        assertEquals(correlationID.toString(),commandHeaders.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(0,commandHeaders.customKeys().size)
        assertEquals(0,commandHeaders.customValues().size)
    }

    @Test
    fun `Given who, a correlation Id and a custom map When I create a new CommandHeaders then the CommandHeaders has this information`(){
        val who ="I don't know"
        val correlationID = UUID.randomUUID()
        val customHeader = mapOf(Pair("key1","value1"),Pair("key2","value2"))
        val commandHeaders = CommandHeaders(who,correlationID,customHeader)

        assertEquals("",commandHeaders.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(commandHeaders.standardValue(HeadersType.WHEN))
        assertEquals(who,commandHeaders.standardValue(HeadersType.WHO))
        assertEquals(correlationID.toString(),commandHeaders.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(2,commandHeaders.customKeys().size)
        assertEquals(2,commandHeaders.customValues().size)
        assertEquals("value1",commandHeaders.customValue("key1"))
        assertEquals("value2",commandHeaders.customValue("key2"))
    }

}