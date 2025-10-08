package io.github.abaddon.kcqrs.core.domain.messages.queries

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

internal class QueryHeadersTest {

    @Test
    fun `Given who and a correlation Id When I create a new QueryHeaders then the QueryHeaders has this information`() {
        val who = "I don't know"
        val correlationID = UUID.randomUUID()
        val queryHeaders = QueryHeaders(who, correlationID)

        assertEquals("", queryHeaders.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(queryHeaders.standardValue(HeadersType.WHEN))
        assertEquals(who, queryHeaders.standardValue(HeadersType.WHO))
        assertEquals(correlationID.toString(), queryHeaders.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(0, queryHeaders.customKeys().size)
        assertEquals(0, queryHeaders.customValues().size)
    }

    @Test
    fun `Given who, a correlation Id and a custom map When I create a new QueryHeaders then the QueryHeaders has this information`() {
        val who = "I don't know"
        val correlationID = UUID.randomUUID()
        val customHeader = mapOf(Pair("key1", "value1"), Pair("key2", "value2"))
        val queryHeaders = QueryHeaders(who, correlationID, customHeader)

        assertEquals("", queryHeaders.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(queryHeaders.standardValue(HeadersType.WHEN))
        assertEquals(who, queryHeaders.standardValue(HeadersType.WHO))
        assertEquals(correlationID.toString(), queryHeaders.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(2, queryHeaders.customKeys().size)
        assertEquals(2, queryHeaders.customValues().size)
        assertEquals("value1", queryHeaders.customValue("key1"))
        assertEquals("value2", queryHeaders.customValue("key2"))
    }
}
