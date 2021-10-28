package io.github.abaddon.kcqrs.core

import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent

interface IAggregate{
    val id: IIdentity;
    val version: Long

    fun applyEvent(event: DomainEvent): IAggregate
    fun uncommittedEvents(): List<DomainEvent>
    fun clearUncommittedEvents()
    //todo snapshot()

}



/*
  public interface IAggregate
  {
    IDomainId Id { get; }
    int Version { get; }
    void ApplyEvent(object @event);
    ICollection GetUncommittedEvents();
    void ClearUncommittedEvents();
    IMemento GetSnapshot();
  }
 */