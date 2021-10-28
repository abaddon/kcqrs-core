package io.github.abaddon.kcqrs.tests.counteraggregate.entities

import io.github.abaddon.kcqrs.core.domain.*
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent
import io.github.abaddon.kcqrs.core.exceptions.UnsupportedEventException
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterInitialisedEvent

data class CounterAggregateRoot private constructor(
    override val id: CounterAggregateId,
    override val version: Long,
    val counter: Int
) : AggregateRoot(RegistrationEventRouter()) {

    companion object{

        fun initialiseCounter(id: CounterAggregateId, counter: Int): CounterAggregateRoot {
            val emptyAggregate = createEmpty()
            emptyAggregate.raiseEvent(CounterInitialisedEvent(id,counter))
            return emptyAggregate
        }

        fun createEmpty(): CounterAggregateRoot {
            return CounterAggregateRoot(CounterAggregateId.create(),0L,0)
        }
    }

    fun increaseCounter(increment: Int) : CounterAggregateRoot{
        try{
            val newCounter = counter + increment
            raiseEvent(CounterIncreasedEvent(id,increment))
        }catch (e: Exception){
            throw e
        }finally {
            return this
        }
    }

    override fun applyEvent(event: DomainEvent): CounterAggregateRoot {
        return when(event){
            is CounterInitialisedEvent -> apply(event)
            is CounterIncreasedEvent -> apply(event)
            is CounterDecreaseEvent -> apply(event)
            else -> throw UnsupportedEventException(event::class.java)
        }
    }

    private fun apply(event: CounterInitialisedEvent): CounterAggregateRoot {
        return copy(id = event.aggregateId ,version = version+1,  counter = event.value)
    }

    private fun apply(event: CounterIncreasedEvent): CounterAggregateRoot {
        check(id == event.aggregateId) { "AggregateId mismatch"}
        val newCounter = counter + event.value;
        return copy(counter = newCounter, version = version+1)
    }

    private fun apply(event: CounterDecreaseEvent): CounterAggregateRoot {
        check(id == event.aggregateId) { "AggregateId mismatch"}
        val newCounter = counter - event.value;
        return copy(counter = newCounter, version = version+1)
    }


}
