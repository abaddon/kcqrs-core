package io.github.abaddon.kcqrs.core.domain.messages

import java.util.*

interface IMessage {
    val messageId: UUID;
}