package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate

sealed class Result<out TException: Exception, out TAggregate: IAggregate> {
    data class Invalid<TException: Exception>(val err: TException): Result<TException, Nothing>()
    data class Valid<TAggregate: IAggregate>(val value: TAggregate): Result<Nothing, TAggregate>()
}
