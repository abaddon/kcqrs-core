package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import java.time.Instant
import java.util.*

data class EventHeader private constructor(
    val standard: Map<String, String>,
    val custom: Map<String, String>
) {

    fun standardValue(headersType: HeadersType): String = standard[headersType.label].orEmpty()

    fun customValues(): Collection<String> = custom.values

    fun customKeys(): Set<String> = custom.keys

    fun customValue(customHeaderKey: String): String = custom[customHeaderKey].orEmpty()

    fun withCustomHeader(customHeader: Map<String, String>): EventHeader = copy(custom = customHeader);

    fun withCorrelationId(correlationId: UUID): EventHeader =
        copy(standard = standard + mapOf(Pair(HeadersType.CORRELATION_ID.label, correlationId.toString())));

    fun withWho(who: String): EventHeader = copy(standard = standard + mapOf(Pair(HeadersType.WHO.label, who)));

    companion object{
        fun create(aggregateRoot: String): EventHeader = EventHeader(
            mapOf<String, String>(
                Pair(HeadersType.EVENT_TYPE.label, aggregateRoot),
                Pair(HeadersType.WHEN.label, Instant.now().epochSecond.toString())
            ), mapOf()
        )

        fun create (aggregateRoot: String, custom: Map<String, String>) : EventHeader = EventHeader(
            mapOf<String, String>(
                Pair(HeadersType.EVENT_TYPE.label, aggregateRoot),
                Pair(HeadersType.WHEN.label, Instant.now().epochSecond.toString())
            ), custom
        )
    }
}




