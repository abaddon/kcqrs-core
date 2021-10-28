package io.github.abaddon.kcqrs.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.exceptions.HandlerForDomainEventNotFoundException

inline  fun IAggregate.throwHandlerNotFound(event: DomainEvent){
    val aggregateName = this.javaClass.simpleName;
    val eventName = event.javaClass.simpleName;
    val exceptionMessage = "Aggregate of type $aggregateName raised an event of type $eventName but no handler could be found to handle the message."
    throw HandlerForDomainEventNotFoundException(exceptionMessage);
}



