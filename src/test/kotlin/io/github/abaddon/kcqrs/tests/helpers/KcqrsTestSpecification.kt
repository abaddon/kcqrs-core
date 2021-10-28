package io.github.abaddon.kcqrs.tests.helpers

import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class KcqrsTestSpecification<CMD : ICommand> {
    val repository: InMemoryEventRepository = InMemoryEventRepository()
    private val expectedException: Exception? = expectedException();

    abstract fun expectedException(): Exception?

    abstract fun given(): List<DomainEvent>

    abstract fun `when`(): CMD

    abstract fun expected(): List<DomainEvent>

    abstract fun onHandler(): ICommandHandler<CMD>

    @Test
    fun checkBehaviour() {
        repository.applyGivenEvents(given())
        var handler = onHandler()

        runBlocking {
            handler.handle(`when`())
        }
        val expected = expected()
        val published = repository.events

        try {
            compareEvents(expected, published)
        } catch (e: Exception) {
            if (expectedException == null)
                assertTrue(false, "${e.javaClass.simpleName}: ${e.message}\n${e.stackTraceToString()} ")
            assertEquals(
                e.javaClass.simpleName,
                expectedException?.javaClass?.simpleName,
                "Exception type  ${e.javaClass.simpleName} differs from expected type ${expectedException?.javaClass?.simpleName}"
            )
            assertEquals(
                e.message,
                expectedException?.message,
                "Exception message  ${e.message} differs from expected type ${e.message}"
            )
        }

    }

    companion object {
        fun compareEvents(expected: List<DomainEvent>, published: List<DomainEvent>) {
            assertEquals(expected.count(), published.count(), "Different number of expected/published events.")

            val eventPairs = expected.zip(published) { e, p -> mapOf(Pair("expected", e), Pair("published", p)) }
            eventPairs.forEach { eventPair ->
                val result = eventPair["expected"] == eventPair["published"]
                //TODO add a compareObject
                assertTrue(
                    result,
                    "Events ${eventPair["expected"]} and ${eventPair["published"]} are different"
                )
            }
        }
    }
}