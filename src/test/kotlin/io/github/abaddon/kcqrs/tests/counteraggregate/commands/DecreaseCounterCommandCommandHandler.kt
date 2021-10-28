package io.github.abaddon.kcqrs.tests.counteraggregate.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.CommandHandler
import io.github.abaddon.kcqrs.core.persistence.IRepository
import io.github.abaddon.kcqrs.tests.counteraggregate.entities.CounterAggregateRoot
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.typeOf

class DecreaseCounterCommandCommandHandler(repository: IRepository) : CommandHandler<DecreaseCounterCommand>(
    repository,
    LoggerFactory.getLogger(DecreaseCounterCommandCommandHandler::class.java.simpleName)
) {
    override suspend fun handle(command: DecreaseCounterCommand) {
        val aggregate = repository.getById(command.aggregateID, CounterAggregateRoot::class)
        aggregate.decreaseCounter(command.value)
        repository.save(aggregate.uncommittedEvents(), UUID.randomUUID(), mapOf())
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