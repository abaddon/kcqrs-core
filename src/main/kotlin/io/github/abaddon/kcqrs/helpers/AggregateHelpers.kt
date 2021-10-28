package io.github.abaddon.kcqrs.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.exceptions.HandlerForDomainEventNotFoundException

//public static void ThrowHandlerNotFound(this IAggregate aggregate, object eventMessage)
//{
//    var exceptionMessage = "Aggregate of type '{0}' raised an event of type '{1}' but no handler could be found to handle the message.".FormatWith(aggregate.GetType().Name, eventMessage.GetType().Name);
//    throw new HandlerForDomainEventNotFoundException(exceptionMessage);
///}

inline  fun IAggregate.throwHandlerNotFound(event: DomainEvent){
    val aggregateName = this.javaClass.simpleName;
    val eventName = event.javaClass.simpleName;
    val exceptionMessage = "Aggregate of type $aggregateName raised an event of type $eventName but no handler could be found to handle the message."
    throw HandlerForDomainEventNotFoundException(exceptionMessage);
}


/*
inline fun <reified T> Any?.tryCast():Boolean {
    println("TEST: this == T ? ${this?.javaClass?.simpleName} == ${
        typeOf<T>().javaClass.simpleName}")
    if (this is T) {
        return true
    }
    return false
}
 */

/*
fun Iterable<TicketEvent>.fold(): TicketState =
  this.fold(InitialState as TicketState) { acc, e -> acc.combine(e) }
 */
//
//fun <T:IAggregate, E:DomainEvent>Iterable<DomainEvent>.fold(): T =
//    this.fold(initial){acc, e -> acc.combine(e)}


