package io.github.abaddon.kcqrs.core.sample.counter.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.CommandHandler
import io.github.abaddon.kcqrs.core.persistence.IRepository
import io.github.abaddon.kcqrs.core.sample.counter.entities.CounterAggregateRoot
import org.slf4j.LoggerFactory
import java.util.*

class IncreaseCounterCommandCommandHandler(repository: IRepository<CounterAggregateRoot>) : CommandHandler<IncreaseCounterCommand, CounterAggregateRoot>(
    repository,
    LoggerFactory.getLogger(IncreaseCounterCommandCommandHandler::class.java.simpleName)
) {
    override suspend fun handle(command: IncreaseCounterCommand) {
        val aggregate = repository.getById(command.aggregateID)
        val updatedAggregate=aggregate.increaseCounter(command.value)
        repository.save(updatedAggregate, UUID.randomUUID(), mapOf())
    }

}

/*
{
      try
      {
          var entity = DailyProgramming.CreateDailyProgramming((DailyProgrammingId)command.AggregateId, command.MovieId, command.ScreenId, command.Date, command.Seats, command.MovieTitle, command.ScreenName);
          await Repository.Save(entity, Guid.NewGuid(), headers => { });
      }
      catch (Exception e)
      {
        Logger.LogError($"CreateDailyProgrammingCommand: Error processing the command: {e.Message} - StackTrace: {e.StackTrace}");
        throw;
      }
    }
 */