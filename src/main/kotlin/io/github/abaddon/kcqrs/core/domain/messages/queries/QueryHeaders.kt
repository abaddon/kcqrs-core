package io.github.abaddon.kcqrs.core.domain.messages.queries

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import java.time.Instant
import java.util.UUID

data class QueryHeaders(
    val standard: Map<String, String>,
    val custom: Map<String, String>
) {

    constructor (who: String, correlationId: UUID) : this(
        who,
        correlationId,
        mapOf<String, String>()
    )

    constructor (who: String, correlationId: UUID, custom: Map<String, String>) : this(
        mapOf<String, String>(
            Pair(HeadersType.WHEN.label, Instant.now().epochSecond.toString()),
            Pair(HeadersType.WHO.label, who),
            Pair(HeadersType.CORRELATION_ID.label, correlationId.toString())
        ), custom
    )

    fun standardValue(headersType: HeadersType): String = standard[headersType.label].orEmpty()

    fun customValues(): Collection<String> = custom.values

    fun customKeys(): Set<String> = custom.keys

    fun customValue(customHeaderKey: String): String = custom[customHeaderKey].orEmpty()
}
