package io.github.abaddon.kcqrs.core.domain.messages.events

internal class DomainEventTest {

//    @Test
//    fun `aaa`(){
//        val expectedDummyEvent = DummyEvent.create(DummyAggregateId(UUID.randomUUID()))
//
//
//        val json = Json.encodeToString(expectedDummyEvent)
//        val actual = DummyEvent.deserialize(json)
//        //val actualDummyEvent = mapper.readValue<DummyEvent>(json)
//        assertEquals(expectedDummyEvent.aggregateId,actual.aggregateId)
//
//    }
//
//
//    @kotlinx.serialization.Serializable
//    data class DummyAggregateId(@kotlinx.serialization.Serializable(with = UUIDSerializer::class)val value: UUID) : IIdentity {
//
//        constructor (): this(UUID.randomUUID())
//
//        override fun valueAsString(): String {
//            return value.toString()
//        }
//    }
//
//    @kotlinx.serialization.Serializable
//    data class DummyEvent(
//        @kotlinx.serialization.Serializable(with = UUIDSerializer::class)
//        override val messageId: UUID,
//        override val aggregateId: DummyAggregateId,
//        override val aggregateType: String,
//        override val header: EventHeader,
//        override val version: Int
//    ) : DomainEvent<DummyEvent> {
//
//        companion object {
//            fun create(aggregateId: DummyAggregateId) : DummyEvent =  DummyEvent(UUID.randomUUID(), aggregateId, "DummyAggregate", EventHeader.create("DummyAggregate"), 1)
//        }
//
//        override fun serialise(item: DummyEvent): String = Json.encodeToString(item)
//
//        override fun deserialize(json: String): DummyEvent = Json.decodeFromString<DummyEvent>(json)
//
//
//    }
}

//class DummyEventSerializer : DomainEventSerializer<DummyEvent>{
//    override fun serialise(item : DummyEvent): String = Json.encodeToString(item)
//    override fun deserialize(json: String): DummyEvent = Json.decodeFromString<DummyEvent>(json)
//}

//interface  DomainEventSerializer<T : DomainEvent>{
//    fun serialise(item : T): String
//    fun deserialize(json: String): T
//}