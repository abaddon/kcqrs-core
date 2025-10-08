package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.domain.messages.queries.IQuery
import io.github.abaddon.kcqrs.core.helpers.LoggerFactory.log

class SimpleQueryHandler<TResult> : IQueryHandler<TResult> {

    override suspend fun handle(query: IQuery<TResult>): Result<TResult> =
        handle(query) { mapOf<String, String>() }

    override suspend fun handle(
        query: IQuery<TResult>,
        updateHeaders: () -> Map<String, String>
    ): Result<TResult> {
        log.debug("Query ${query::class.java.simpleName} execution started")
        return query.execute().onSuccess {
            log.debug("Query ${query::class.java.simpleName} completed successfully")
        }.onFailure { error ->
            log.error("Query ${query::class.java.simpleName} failed: ${error.message}", error)
        }
    }
}
