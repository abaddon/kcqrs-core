package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class InMemoryProjectionRepositoryTest{


    @Test
    fun `given an unavailable projection  when I load it then an empty projection is returned`() = runBlocking{
        val projectionRepository = InMemoryProjectionRepository<DummyProjection>(){
            DummyProjection(it as DummyProjectionKey,0)
        }

        val key = DummyProjectionKey("key1")

        val actualProjection = projectionRepository.getByKey(key)
        val expectedProjection = DummyProjection(key,0)

        assertEquals(expectedProjection,actualProjection)
    }

    @Test
    fun `given a new projection  when I save it then the projection is persisted`() = runBlocking{
        val projectionRepository = InMemoryProjectionRepository<DummyProjection>(){
            DummyProjection(it as DummyProjectionKey,0)
        }

        val key = DummyProjectionKey("key1")
        val expectedProjection = DummyProjection(key,4)

        projectionRepository.save(expectedProjection,0)

        val actualProjection = projectionRepository.getByKey(key)


        assertEquals(expectedProjection,actualProjection)
    }

    data class DummyProjectionKey(val name: String ): IProjectionKey {
        override fun key(): String = name

    }
    data class DummyProjection(override val key: DummyProjectionKey,val receivedEvents: Int =0): IProjection {
        override fun applyEvent(event: IDomainEvent): DummyProjection {
            return copy(receivedEvents = receivedEvents+1)
        }
    }
}