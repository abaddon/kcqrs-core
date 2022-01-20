package io.github.abaddon.kcqrs.eventstores.eventstoredb

//import io.github.abaddon.kcqrs.eventstores.eventstoredb.serializations.UUIDSerializer
//import kotlinx.serialization.Serializable
import com.eventstore.dbclient.Position
import com.eventstore.dbclient.RecordedEvent
import com.eventstore.dbclient.StreamRevision
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*


internal class HelpersKtTest {
    @Test
    fun `Given SimpleDomainEvent When ToEventData Then EventData Created`() {

        val aggregateId = DummyAggregateId(UUID.randomUUID())
        val name = "name_1"
        val expectedDummyDomainEvent = DummyDomainEvent.create(aggregateId, name);

        val headers = mapOf<String, String>(
            Pair("header1", "value1")
        )
        //
//        val eventId = expectedDummyDomainEvent.messageId;
//        val eventType = expectedDummyDomainEvent::class.qualifiedName!!
//        //val
//        val eventJson = mapper.writeValueAsString(expectedDummyDomainEvent)//Json.encodeToString<T>(this) //
//
//        val headerJson = mapper.writeValueAsString(headers)//Json.encodeToString(header) //
//        val dummyEventDate = EventData(
//            eventId,
//            eventType,
//            "application/json",
//            eventJson.encodeToByteArray(),
//            headerJson.encodeToByteArray()
//        )
        //
        val dummyEventDate = expectedDummyDomainEvent.toEventData(headers)

        //build RecordedEvent from simpleEventDate
        val systemMap = mapOf<String, String>(
            Pair("type", DummyDomainEvent::class.qualifiedName!!),
            Pair("content-type", "json"),
            Pair("created", Instant.now().toEpochMilli().toString()),
        )
        val eventStoreRecordedEvent = RecordedEvent(
            "1223",
            StreamRevision(1L),
            dummyEventDate.eventId,
            Position(3L, 2L),
            systemMap,
            dummyEventDate.eventData,
            dummyEventDate.userMetadata
        );

        //deserialize
        val actualDummyDomainEvent = eventStoreRecordedEvent.toDomainEvent()
        //
//        val eventTypeName = eventStoreRecordedEvent.eventType
//
//        val eventClass = Class.forName(eventTypeName)
//
//
//        val eventDataJson: String = eventStoreRecordedEvent.eventData.decodeToString()
//        val eventMetaJson: String = eventStoreRecordedEvent.userMetadata.decodeToString() //TODO headers not managed
//
//        val actualDummyDomainEvent = mapper.readValue(eventDataJson, eventClass)
        //

        //assertEquals(expectedDummyDomainEvent.aggregateId, actualDummyDomainEvent.aggregateId)
        assertEquals(expectedDummyDomainEvent.name, (actualDummyDomainEvent as DummyDomainEvent).name)

    }

}


class DummyAggregateId constructor(
    val value: UUID
) : IIdentity {
    override fun valueAsString(): String {
        return value.toString()
    }
}


data class DummyAggregate(
    override val id: IIdentity,
    override val version: Long,
    override val uncommittedEvents: MutableCollection<DomainEvent>
) : AggregateRoot() {
    companion object
}

data class DummyDomainEvent private constructor(
    override val messageId: UUID,
    override val aggregateId: DummyAggregateId = DummyAggregateId(UUID.randomUUID()),
    override val aggregateType: String = DummyAggregate.javaClass.simpleName,
    override val version: Int = 1,
    override val header: EventHeader,
    val name: String,
): DomainEvent {

    companion object {
        fun create(aggregateId: DummyAggregateId, name: String): DummyDomainEvent {
            val aggregateType = "DummyAggregate"
            val header = EventHeader.create(aggregateType)
            return DummyDomainEvent(UUID.randomUUID(), aggregateId, aggregateType, 1,header, name)
        }
    }
}
