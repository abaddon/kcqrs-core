package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.examples.counterInMemory.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.repository.CounterAggregateRepository
import kotlinx.coroutines.runBlocking


fun main() {
    val repository = CounterAggregateRepository()
    val aggregateHandler = CounterAggregateHandler(repository)
    val counterAggregateId = CounterAggregateId()
    val expectedFinalVersion = 4

    val commands = listOf(
        InitialiseCounterCommand(counterAggregateId, 0),
        IncreaseCounterCommand(counterAggregateId, 1),
        IncreaseCounterCommand(counterAggregateId, 1),
        IncreaseCounterCommand(counterAggregateId, 1),
        DecreaseCounterCommand(counterAggregateId, 2),
        IncreaseCounterCommand(counterAggregateId, 3)
    )

    runBlocking {
        commands.forEach { command ->
            aggregateHandler.handle(command)
        }
    }

    runBlocking {
        val result = repository.getById(counterAggregateId)
        requireNotNull(result)
        println("result id ${result.id.value} has to be equal to ${counterAggregateId.value}: ${result.id.value == counterAggregateId.value}")
        println("final value is: ${result.counter} and should be $expectedFinalVersion: ${result.counter == expectedFinalVersion}")
    }

}


