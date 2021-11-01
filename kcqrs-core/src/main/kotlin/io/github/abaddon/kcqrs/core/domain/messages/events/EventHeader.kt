package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

data class EventHeader private constructor(
    private val standard: Map<String, String>,
    private val custom: Map<String, String>
) {
    constructor (kclass: KClass<*>) : this(
        mapOf<String, String>(
            Pair(HeadersType.EVENT_TYPE.label, kclass.simpleName!!),
            Pair(HeadersType.WHEN.label, Instant.now().epochSecond.toString())
        ), mapOf()
    )

    fun standardValue(headersType: HeadersType): String = standard[headersType.label].orEmpty()

    fun customValues(): Collection<String> = custom.values

    fun customKeys(): Set<String> = custom.keys

    fun customValue(customHeaderKey: String): String = custom[customHeaderKey].orEmpty()

    fun withCustomHeader(customHeader: Map<String, String>): EventHeader = copy(custom = customHeader);

    fun withCorrelationId(correlationId: UUID): EventHeader =
        copy(standard = standard + mapOf(Pair(HeadersType.CORRELATION_ID.label, correlationId.toString())));

    fun withWho(who: String): EventHeader = copy(standard = standard + mapOf(Pair(HeadersType.WHO.label, who)));
}




