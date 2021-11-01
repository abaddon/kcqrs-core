package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent

fun <TAggregate : IAggregate> Iterable<IEvent>.foldEvents(initial: TAggregate): TAggregate {
    var accumulator = initial
    for (element in this) {
        accumulator = accumulator.applyEvent(element) as TAggregate
    }
    return accumulator
}