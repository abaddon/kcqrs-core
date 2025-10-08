package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.domain.messages.queries.IQuery

interface IQueryHandler<TResult> {
    suspend fun handle(query: IQuery<TResult>): Result<TResult>

    suspend fun handle(
        query: IQuery<TResult>,
        updateHeaders: () -> Map<String, String>
    ): Result<TResult>
}
