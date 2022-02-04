package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.IAggregateHandler
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kustomCompare.CompareLogic
import io.github.abaddon.kustomCompare.config.CompareLogicConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class KcqrsTestSpecification<TAggregate:IAggregate>(kClass: KClass<TAggregate>) {
    val repository: InMemoryEventRepository<TAggregate> = InMemoryEventRepository<TAggregate>(kClass)
    private val expectedException: Exception? = expectedException();

    abstract fun expectedException(): Exception?

    abstract fun given(): List<IDomainEvent>

    abstract fun `when`(): ICommand<TAggregate>

    abstract fun expected(): List<IDomainEvent>

    abstract fun onHandler(): IAggregateHandler<TAggregate>

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
        fun compareEvents(expected: List<IDomainEvent>, published: List<IDomainEvent>) {
            assertEquals(expected.count(), published.count(), "Different number of expected/published events.")
            val compareLogic = CompareLogic(CompareLogicConfig()
                .addMemberToIgnore("messageId")
            )
            val eventPairs = expected.zip(published) { e, p -> mapOf(Pair("expected", e), Pair("published", p)) }
            eventPairs.forEach { eventPair ->

                val result = compareLogic.compare(eventPair["expected"]!!,eventPair["published"]!!)
                assertTrue(
                    result.result(),
                    "Events ${eventPair["expected"]} and ${eventPair["published"]} are different"
                )
            }
        }
    }
}