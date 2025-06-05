package io.github.abaddon.kcqrs.core.domain.messages

import java.util.UUID

interface IMessage {
    val messageId: UUID
}