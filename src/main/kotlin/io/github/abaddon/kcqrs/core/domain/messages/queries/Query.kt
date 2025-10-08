package io.github.abaddon.kcqrs.core.domain.messages.queries

import java.time.Instant
import java.util.UUID

abstract class Query<TResult> private constructor(
    override val messageId: UUID,
    val queryHeaders: QueryHeaders,
    val queryDate: Instant = Instant.now()
) : IQuery<TResult> {

    protected constructor(who: String = "anonymous") : this(
        UUID.randomUUID(),
        QueryHeaders(who, UUID.randomUUID())
    )
}
