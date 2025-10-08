package io.github.abaddon.kcqrs.core.domain.messages.queries

import io.github.abaddon.kcqrs.core.domain.messages.IMessage

interface IQuery<TResult> : IMessage {
    suspend fun execute(): Result<TResult>
}
