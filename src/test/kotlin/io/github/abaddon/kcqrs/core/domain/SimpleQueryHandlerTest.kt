package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.domain.messages.queries.Query
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SimpleQueryHandlerTest {

    @Test
    fun `Given a query when handle is called then query is executed successfully`() = runTest {
        val handler = SimpleQueryHandler<String>()
        val expectedResult = "test-result"
        val query = SuccessfulQuery(expectedResult)

        val result = handler.handle(query)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `Given a failing query when handle is called then failure is returned`() = runTest {
        val handler = SimpleQueryHandler<String>()
        val errorMessage = "Query execution failed"
        val query = FailingQuery(errorMessage)

        val result = handler.handle(query)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun `Given a query with updateHeaders when handle is called then query is executed`() = runTest {
        val handler = SimpleQueryHandler<String>()
        val expectedResult = "test-result-with-headers"
        val query = SuccessfulQuery(expectedResult)
        val updateHeaders: () -> Map<String, String> = { mapOf("custom" to "header") }

        val result = handler.handle(query, updateHeaders)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `Given multiple queries when handle is called then each returns its own result`() = runTest {
        val handler = SimpleQueryHandler<String>()
        val query1 = SuccessfulQuery("result-1")
        val query2 = SuccessfulQuery("result-2")

        val result1 = handler.handle(query1)
        val result2 = handler.handle(query2)

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals("result-1", result1.getOrNull())
        assertEquals("result-2", result2.getOrNull())
    }

    @Test
    fun `Given a query returning complex type when handle is called then complex result is returned`() = runTest {
        val handler = SimpleQueryHandler<ComplexResult>()
        val expectedResult = ComplexResult("data", 42)
        val query = ComplexQuery(expectedResult)

        val result = handler.handle(query)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    private class SuccessfulQuery(private val resultToReturn: String) : Query<String>() {
        override suspend fun execute(): Result<String> {
            return Result.success(resultToReturn)
        }
    }

    private class FailingQuery(private val errorMessage: String) : Query<String>() {
        override suspend fun execute(): Result<String> {
            return Result.failure(Exception(errorMessage))
        }
    }

    private class ComplexQuery(private val resultToReturn: ComplexResult) : Query<ComplexResult>() {
        override suspend fun execute(): Result<ComplexResult> {
            return Result.success(resultToReturn)
        }
    }

    private data class ComplexResult(val name: String, val count: Int)
}
