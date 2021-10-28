package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.HeadersType
import java.time.Instant
import java.util.*

data class CommandHeaders private constructor(
    val standard: Map<String,String>,
    val custom: Map<String,String>
){

    constructor (who: String, identity: IIdentity,correlationId: UUID) : this(who, identity,correlationId, mapOf<String,String>());
    constructor (who: String, identity: IIdentity,correlationId: UUID, custom: Map<String,String>) : this(standardHandlerBuilder(who,correlationId),custom) {
    }

    companion object{
        private fun standardHandlerBuilder(who: String, correlationId: UUID): Map<String,String>{
            return mapOf<String,String>(
                Pair(HeadersType.WHEN.label,Instant.now().epochSecond.toString()),
                Pair(HeadersType.WHO.label,who),
                Pair(HeadersType.CORRELATION_ID.label,correlationId.toString())
            )

        }
    }
}




