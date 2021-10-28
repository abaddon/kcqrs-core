package io.github.abaddon.kcqrs.tests.counteraggregate.entities

import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.tests.counteraggregate.events.DomainErrorEvent

data class CounterAggregateRoot private constructor(
    override val id: CounterAggregateId,
    override val version: Long,
    val counter: Int
) : AggregateRoot() {

    constructor() : this(CounterAggregateId.create(), 0L, 0)

    companion object {

        fun initialiseCounter(id: CounterAggregateId, initialValue: Int): CounterAggregateRoot {
            val emptyAggregate = CounterAggregateRoot()
            try {
                check(initialValue >= 0 && initialValue < Int.MAX_VALUE) { "Value $initialValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }

                emptyAggregate.raiseEvent(CounterInitialisedEvent(id, initialValue))
            } catch (e: Exception) {
                emptyAggregate.raiseEvent(DomainErrorEvent(id, e))
            } finally {
                return emptyAggregate
            }
        }
    }

    fun increaseCounter(incrementValue: Int): CounterAggregateRoot {
        try {
            check(incrementValue >= 0 && incrementValue < Int.MAX_VALUE) { "Value $incrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter + incrementValue
            check(updatedCounter < Int.MAX_VALUE) { "Aggregate value $updatedCounter is not valid, it has to be < ${Int.MAX_VALUE}" }

            raiseEvent(CounterIncreasedEvent(id, incrementValue))
        } catch (e: Exception) {
            raiseEvent(DomainErrorEvent(id, e))
        } finally {
            return this
        }
    }

    fun decreaseCounter(decrementValue: Int): CounterAggregateRoot {
        try {
            check(decrementValue >= 0 && decrementValue < Int.MAX_VALUE) { "Value $decrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter - decrementValue
            check(updatedCounter >= 0) { "Aggregate value $updatedCounter is not valid, it has to be >= 0" }

            raiseEvent(CounterDecreaseEvent(id, decrementValue))
        } catch (e: Exception) {
            raiseEvent(DomainErrorEvent(id, e))
        } finally {
            return this
        }
    }

//    override fun applyEvent(event: DomainEvent): CounterAggregateRoot {
//        return when(event){
//            is CounterInitialisedEvent -> apply(event)
//            is CounterIncreasedEvent -> apply(event)
//            is CounterDecreaseEvent -> apply(event)
//            else -> throw UnsupportedEventException(event::class.java)
//        }
//    }

    private fun apply(event: CounterInitialisedEvent): CounterAggregateRoot {
        return copy(id = event.aggregateId, version = version + 1, counter = event.value)
    }

    private fun apply(event: CounterIncreasedEvent): CounterAggregateRoot {
        val newCounter = counter + event.value;
        return copy(counter = newCounter, version = version + 1)
    }

    private fun apply(event: CounterDecreaseEvent): CounterAggregateRoot {
        val newCounter = counter - event.value;
        return copy(counter = newCounter, version = version + 1)
    }


}
