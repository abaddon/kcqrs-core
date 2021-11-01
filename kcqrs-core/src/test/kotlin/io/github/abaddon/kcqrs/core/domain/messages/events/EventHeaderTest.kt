package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*


internal class EventHeaderTest{

    @Test
    fun `Given an aggregate class when create an EventHeader then the EventHeader contains the aggregate class name`(){
        val eventHeader = EventHeader(AggregateRoot::class)
        val eventTypeExpected = "AggregateRoot"

        assertEquals(eventTypeExpected,eventHeader.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(eventHeader.standardValue(HeadersType.WHEN))
        assertEquals("",eventHeader.standardValue(HeadersType.WHO))
        assertEquals("",eventHeader.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(0,eventHeader.customKeys().size)
        assertEquals(0,eventHeader.customValues().size)

    }

    @Test
    fun `Given a correlationId when add it to the EventHeader then the EventHeader has the same correlationId`(){
        val kClass = AggregateRoot::class;
        val correlationId = UUID.randomUUID()
        val eventHeader = EventHeader(kClass)
            .withCorrelationId(correlationId)

        val eventTypeExpected = kClass.simpleName!!

        assertEquals(eventTypeExpected,eventHeader.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(eventHeader.standardValue(HeadersType.WHEN))
        assertEquals("",eventHeader.standardValue(HeadersType.WHO))
        assertEquals(correlationId.toString(),eventHeader.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(0,eventHeader.customKeys().size)
        assertEquals(0,eventHeader.customValues().size)

    }

    @Test
    fun `Given a "who" when add it to the EventHeader then the EventHeader has the same "who"`(){
        val kClass = AggregateRoot::class;
        val correlationId = UUID.randomUUID()
        val who = "I don't know"
        val eventHeader = EventHeader(kClass)
            .withCorrelationId(correlationId)
            .withWho(who)

        val eventTypeExpected = kClass.simpleName!!

        assertEquals(eventTypeExpected,eventHeader.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(eventHeader.standardValue(HeadersType.WHEN))
        assertEquals(who,eventHeader.standardValue(HeadersType.WHO))
        assertEquals(correlationId.toString(),eventHeader.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(0,eventHeader.customKeys().size)
        assertEquals(0,eventHeader.customValues().size)

    }

    @Test
    fun `Given a custom headers when add it to the EventHeader then the EventHeader has the same custom header`(){
        val kClass = AggregateRoot::class;
        val correlationId = UUID.randomUUID()
        val customHeader = mapOf(Pair("key1", "value1"),Pair("key2", "value2"))
        val who = "I don't know"
        val eventHeader = EventHeader(kClass)
            .withCorrelationId(correlationId)
            .withWho(who)
            .withCustomHeader(customHeader)

        val eventTypeExpected = kClass.simpleName!!

        assertEquals(eventTypeExpected,eventHeader.standardValue(HeadersType.EVENT_TYPE))
        assertNotNull(eventHeader.standardValue(HeadersType.WHEN))
        assertEquals(who,eventHeader.standardValue(HeadersType.WHO))
        assertEquals(correlationId.toString(),eventHeader.standardValue(HeadersType.CORRELATION_ID))

        assertEquals(2,eventHeader.customKeys().size)
        assertEquals(2,eventHeader.customValues().size)
        assertEquals("value1",eventHeader.customValue("key1"))
        assertEquals("value2",eventHeader.customValue("key2"))

    }
}