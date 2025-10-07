package io.github.abaddon.kcqrs.core.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ResultHelpersTest {

    @Test
    fun `Given a successful result when flatMap with successful transform then return transformed success`() {
        val result = Result.success(5)
        val transformed = result.flatMap { value -> Result.success(value * 2) }

        assertTrue(transformed.isSuccess)
        assertEquals(10, transformed.getOrNull())
    }

    @Test
    fun `Given a successful result when flatMap with failing transform then return failure`() {
        val result = Result.success(5)
        val exception = RuntimeException("Transform failed")
        val transformed = result.flatMap<Int, Int> { Result.failure(exception) }

        assertTrue(transformed.isFailure)
        assertEquals(exception, transformed.exceptionOrNull())
    }

    @Test
    fun `Given a failed result when flatMap then transform is not called and failure is propagated`() {
        val exception = RuntimeException("Original failure")
        val result = Result.failure<Int>(exception)
        var transformCalled = false

        val transformed = result.flatMap { value ->
            transformCalled = true
            Result.success(value * 2)
        }

        assertTrue(transformed.isFailure)
        assertEquals(false, transformCalled)
        assertEquals(exception, transformed.exceptionOrNull())
    }

    @Test
    fun `Given successful result when chaining multiple flatMaps then all transforms are applied`() {
        val result = Result.success(2)
        val transformed = result
            .flatMap { value -> Result.success(value + 3) }  // 2 + 3 = 5
            .flatMap { value -> Result.success(value * 2) }  // 5 * 2 = 10
            .flatMap { value -> Result.success(value.toString()) } // "10"

        assertTrue(transformed.isSuccess)
        assertEquals("10", transformed.getOrNull())
    }

    @Test
    fun `Given chained flatMaps when one fails then subsequent transforms are not called`() {
        val result = Result.success(2)
        var transform1Called = false
        var transform2Called = false
        var transform3Called = false

        val exception = RuntimeException("Middle failure")
        val transformed = result
            .flatMap { value ->
                transform1Called = true
                Result.success(value + 3)
            }
            .flatMap<Int, Int> {
                transform2Called = true
                Result.failure(exception)
            }
            .flatMap { value ->
                transform3Called = true
                Result.success(value * 2)
            }

        assertTrue(transform1Called)
        assertTrue(transform2Called)
        assertEquals(false, transform3Called)
        assertTrue(transformed.isFailure)
        assertEquals(exception, transformed.exceptionOrNull())
    }

    @Test
    fun `Given successful result when flatMap transforms to different type then type is changed`() {
        val result = Result.success(42)
        val transformed = result.flatMap { value -> Result.success("Number: $value") }

        assertTrue(transformed.isSuccess)
        assertEquals("Number: 42", transformed.getOrNull())
    }

    @Test
    fun `Given successful result when flatMap transforms to complex type then complex type is returned`() {
        data class Person(val name: String, val age: Int)

        val result = Result.success(25)
        val transformed = result.flatMap { age -> Result.success(Person("Alice", age)) }

        assertTrue(transformed.isSuccess)
        assertEquals(Person("Alice", 25), transformed.getOrNull())
    }
}
