package io.github.abaddon.kcqrs.eventstores.eventstoredb

import com.eventstore.dbclient.Position
import com.eventstore.dbclient.RecordedEvent
import com.eventstore.dbclient.StreamRevision
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*


internal class HelpersKtTest {
    @Test
    fun `Given SimpleDomainEvent When ToEventData Then EventData Created`() {

        val aggregateId = SimpleAggregateId(UUID.randomUUID())
        val name = "name_1"
        val simpleDomainEvent = SimpleFakeDomainEvent(aggregateId,name);

        val headers = mapOf<String, String>(
            Pair("header1", "value1")
        )

        val simpleEventDate = simpleDomainEvent.toEventData(headers)

        //build RecordedEvent from simpleEventDate
        val systemMap = mapOf<String,String>(
            Pair("type", SimpleFakeDomainEvent::class.qualifiedName!!),
            Pair("content-type","json"),
            Pair("created",Instant.now().toEpochMilli().toString()),
        )
        val recordedEvent = RecordedEvent("1223", StreamRevision(1L),simpleEventDate.eventId, Position(3L,2L), systemMap,simpleEventDate.eventData,simpleEventDate.userMetadata);

        //deserialize
        val deserializedEvent = recordedEvent.toDomainEvent()

        assertEquals(simpleDomainEvent.aggregateId,deserializedEvent.aggregateId)
        assertEquals(simpleDomainEvent.name,(deserializedEvent as SimpleFakeDomainEvent).name)

    }

}


data class SimpleAggregateId(
    val value: UUID
    ): IIdentity {
    override fun valueAsString(): String {
        return value.toString()
    }
}

data class SimpleFakeDomainEvent private constructor(
    override val messageId: UUID,
    override val aggregateId: SimpleAggregateId,
    override val version: Int = 1,
    val name: String
) : DomainEvent<AggregateRoot>(){

    @OptIn(ExperimentalStdlibApi::class)
    constructor(aggregateId: SimpleAggregateId, name: String): this(UUID.randomUUID(),aggregateId,1,name)

}
