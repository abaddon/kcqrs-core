package io.github.abaddon.kcqrs.core.exceptions

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent

class UnsupportedEventException(eventClass: Class<out DomainEvent>)
    : Exception("Unsupported event ${eventClass.canonicalName}")