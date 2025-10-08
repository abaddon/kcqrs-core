package io.github.abaddon.kcqrs.core.domain.messages.queries

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class QueryTest {

    @Test
    fun `Given who when creating query then query is initialized with correct values`() {
        val who = "test-user"

        val query = TestQuery(who)

        assertEquals(who, query.queryHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
        assertNotNull(query.messageId)
        assertNotNull(query.queryHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID))
        assertNotNull(query.queryDate)
    }

    @Test
    fun `Given no parameters when creating query then who defaults to anonymous`() {
        val query = TestQuery()

        assertEquals("anonymous", query.queryHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
    }

    @Test
    fun `Given two queries created when comparing messageIds then they are different`() {
        val query1 = TestQuery("user1")
        val query2 = TestQuery("user1")

        assertNotEquals(query1.messageId, query2.messageId)
    }

    @Test
    fun `Given two queries created when comparing correlationIds then they are different`() {
        val query1 = TestQuery("user1")
        val query2 = TestQuery("user1")

        assertNotEquals(
            query1.queryHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID),
            query2.queryHeaders.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID)
        )
    }

    @Test
    fun `Given a query when execute is called then result contains expected data`() = runTest {
        val expectedResult = "test-result"
        val query = TestQuery("test-user", expectedResult)

        val result = query.execute()

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `Given query headers when accessing properties then correct values are returned`() {
        val who = "test-user"
        val correlationId = UUID.randomUUID()
        val headers = QueryHeaders(who, correlationId)

        assertEquals(who, headers.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.WHO))
        assertEquals(correlationId.toString(), headers.standardValue(io.github.abaddon.kcqrs.core.domain.messages.HeadersType.CORRELATION_ID))
    }

    private class TestQuery(who: String = "anonymous", private val resultToReturn: String = "default-result") :
        Query<String>(who) {

        override suspend fun execute(): Result<String> {
            return Result.success(resultToReturn)
        }
    }
}
