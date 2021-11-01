package io.github.abaddon.kcqrs.core.domain.messages.commands

import io.github.abaddon.kcqrs.core.IIdentity
import java.time.Instant
import java.util.*

open class Command private constructor(
    override val messageId: UUID,
    override val aggregateID: IIdentity,
    val commandHeaders: CommandHeaders,
    val commitDate: Instant = Instant.now(),

    ) : ICommand {

        protected constructor(aggregateID: IIdentity, who: String ="anonymous" ) : this(UUID.randomUUID(), aggregateID, CommandHeaders(who,UUID.randomUUID()))

}