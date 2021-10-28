package io.github.abaddon.kcqrs.core.domain.messages.events

import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import java.time.Instant
import java.util.*

data class EventHeader private constructor(
    val standard: Map<String,String>,
    val custom: Map<String,String>
){

    constructor (who: String, eventType: String, correlationId: UUID) : this(who, eventType, correlationId, mapOf<String,String>());
    constructor (who: String, eventType: String, correlationId: UUID,custom: Map<String,String>) : this(standardHandlerBuilder(who, eventType,correlationId),custom) {
    }

    companion object{
        private fun standardHandlerBuilder(who: String, eventType: String,correlationId: UUID): Map<String,String>{
            return mapOf<String,String>(
                Pair(HeadersType.WHEN.label,Instant.now().epochSecond.toString()),
                Pair(HeadersType.WHO.label,who),
                Pair(HeadersType.EVENT_TYPE.label,eventType),
                Pair(HeadersType.CORRELATION_ID.label,correlationId.toString())
            )
        }
    }
}




