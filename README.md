# kotlin-cqrs (Kcqrs)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.abaddon.kcqrs/kcqrs-core?versionPrefix=0.&style=flat&label=version&color=green)
[![Java CI with Gradle](https://github.com/abaddon/kotlin-cqrs/actions/workflows/gradle.yml/badge.svg)](https://github.com/abaddon/kotlin-cqrs/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/abaddon/kcqrs-core/branch/main/graph/badge.svg?token=1N8KGK99QV)](https://codecov.io/gh/abaddon/kcqrs-core)

A Kotlin CQRS library based on [C# Muflone library](https://github.com/CQRS-Muflone/Muflone)

### Libraries
- [kcqrs-core](https://github.com/abaddon/kcqrs-core) It contains the main entities like:
  - Entity
  - Aggregate
  - Event
  - Command
  - Projection
  - Command and Projection handlers
  - Aggregate and Projection repositories
- [kcqrs-EventStoreDB](https://github.com/abaddon/kcqrs-EventStoreDB)  EventstoreDB implementation of event store repository and projection handler
- [kcqrs-test](https://github.com/abaddon/kcqrs-test) it offers a simple test suite to test easily: commands, aggregate, and events
- [kcqrs-example](https://github.com/abaddon/kcqrs-example)  Simple examples of how use KCQRS libs

### Architecture
![kcqrs-schema](docs/kcqrs-schema.jpg)

### Getting started

#### Define an Aggregate
The scope of this aggregate is increase or decrease an internal field called `counter` following these business logics:
- counter has to be > 0
- counter has to be < Int.MAX_VALUE

The allowed operation are:
- initialise the counter with the value received
- increase the counter of the value received
- decrease the counter of the value received
The value received has to be a value between 0 and Int.MAX_VALUE, or it will be rejected

```kotlin
/**  Aggregate Identity **/
data class CounterAggregateId(val value: UUID) : IIdentity {
    constructor (): this(UUID.randomUUID())
    override fun valueAsString(): String {
        return value.toString()
    }
}

data class CounterAggregateRoot constructor(
    override val id: CounterAggregateId,
    override val version: Long,
    val counter: Int,
    override val uncommittedEvents: MutableCollection<IDomainEvent>
) : AggregateRoot() {
    private val log = LoggerFactory.getLogger(this::class.simpleName)

    companion object {
        fun initialiseCounter(id: CounterAggregateId, initialValue: Int): CounterAggregateRoot {
            /** Initialisation an empty aggregate **/
            val emptyAggregate = CounterAggregateRoot(id, 0L, 0, ArrayList<IDomainEvent>())
            return try {
                /** Validate incrementValue **/
                check(initialValue >= 0 && initialValue < Int.MAX_VALUE) { "Value $initialValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
                /** Raise the event CounterInitialisedEvent if the initial value is right. The empty aggregate is used only as container for the event generated. **/
                emptyAggregate.raiseEvent(CounterInitialisedEvent(id, initialValue)) as CounterAggregateRoot
            } catch (e: Exception) {
                /** In case of error an error event is generated **/
                emptyAggregate.raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
            }
        }
    }
    
    fun increaseCounter(incrementValue: Int): CounterAggregateRoot {
        return try {
            /** Validate incrementValue **/
            check(incrementValue >= 0 && incrementValue < Int.MAX_VALUE) { "Value $incrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter + incrementValue
            /**  Validate updatedCounter **/
            check(updatedCounter < Int.MAX_VALUE) { "Aggregate value $updatedCounter is not valid, it has to be < ${Int.MAX_VALUE}" }
            /** Raise the event CounterIncreasedEvent if the updatedCounter value is right. **/  
            raiseEvent(CounterIncreasedEvent(id, incrementValue)) as CounterAggregateRoot
        } catch (e: Exception) {
            raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
        }
    }

    fun decreaseCounter(decrementValue: Int): CounterAggregateRoot {
        return try {
            check(decrementValue >= 0 && decrementValue < Int.MAX_VALUE) { "Value $decrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter - decrementValue
            check(updatedCounter >= 0) { "Aggregate value $updatedCounter is not valid, it has to be >= 0" }
            /** Raise the event CounterDecreaseEvent if the updatedCounter value is right. **/
            raiseEvent(CounterDecreaseEvent(id, decrementValue)) as CounterAggregateRoot
        } catch (e: HandlerForDomainEventNotFoundException) {
            raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
        }
    }

    /**
     * AggregateRoot use a EventRouter based on the function name, so you don't need to register for each event the proper function to call.
     * All functions called apply(...) are automatically registered in the Event Route.
     * 
     * The previous methods don't apply any changes on the aggregate, they trigger events only.
     * Events triggered are then apply to the Aggregate using the functions apply(...) below.
     * One apply function for each event to apply to the Aggregate. 
     * The apply function is pretty simple, and it doesn't contain any validation because the events are the source of true.
     **/
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

    private fun apply(event: DomainErrorEvent): CounterAggregateRoot {
        return copy(version = version + 1)
    }

}
```
#### Define commands
Commands are entity used to perform an operation on its Aggregate root.
A Command has to extend the abstract class Command<TAggregateRoot>.
Each command class has to implement the method `execute(aggregate:TAggregateRoot?)`. 
This method contain the business logic of the command, leaving the CommandHandler simpler and generic.

```kotlin
data class InitialiseCounterCommand(
    override val aggregateID: CounterAggregateId, //aggregate identity. Each command has to be linked to only one aggregate instance  
    val value: Int // the values / parameters that the command needs
): Command<CounterAggregateRoot>(aggregateID) {
    /**
     * This method receive the existing aggregate, if exist and then perform the operation on the aggregate
     * In this case the command create a new aggregate, so currentAggregate is null and it's not used 
     */
    override fun execute(currentAggregate: CounterAggregateRoot?): CounterAggregateRoot {
        return CounterAggregateRoot.initialiseCounter(aggregateID, value)
    }
}

data class IncreaseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
): Command<CounterAggregateRoot>(aggregateID) {
    /**
     * In this case the command want to increase the aggregate value, so the currentAggregate has to exist. If it's missing an exception is raised
     * After the currentAggregate validation, the method execute the aggregate method to increase the counter.
     */
    override fun execute(currentAggregate: CounterAggregateRoot?): CounterAggregateRoot {
        requireNotNull(currentAggregate)
        return currentAggregate.increaseCounter(value)
    }
}


```

#### Define an AggregateHandler
The Command Handler scope is to receive a command and execute it.
SimpleAggregateCommandHandler is a Command Handler that should be suitable in most of the cases. 
In the other situation you can implement directly the IAggregateCommandHandler.

SimpleAggregateCommandHandler everytime a new command is coming, try to rehydrate the aggregate calling the repository using as key the aggregateIdentity in the command.
The rehydrate aggregate is used to execute the command.
The output of the execution is a new aggregate that will be saved on the repository.

```kotlin
class SimpleAggregateCommandHandler<TAggregate : IAggregate>(
  override val repository: IAggregateRepository<TAggregate>,
) : IAggregateCommandHandler<TAggregate> {
  
  override suspend fun handle(
    command: ICommand<TAggregate>,
    updateHeaders: () -> Map<String, String>
  ): Result<Exception, TAggregate> =
    when (val actualAggregateResult = repository.getById(command.aggregateID)) {
      is Result.Valid -> {
        val newAggregate = command.execute(actualAggregateResult.value)
        repository.save(newAggregate, UUID.randomUUID(), updateHeaders)
      }
      is Result.Invalid -> actualAggregateResult
    }

  override suspend fun handle(command: ICommand<TAggregate>): Result<Exception, TAggregate> =
    handle(command) { mapOf<String, String>() }
}
```

#### Define a DomainEvent
A domain Event is what we persist to rehydrate an aggregate and represent the source of true.
Each event has to extend the interface IDomainEvent.

Each event has to be related to an aggregate identity (`aggregateId`).
The field `aggregateType` has to contain the name of the aggregateRoot class

```kotlin
data class CounterIncreasedEvent(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    override val version: Int = 1,
    override val aggregateType: String,
    override val header: EventHeader,
    val value: Int,
) : IDomainEvent {
    constructor(aggregateId: CounterAggregateId, value: Int) : this(UUID.randomUUID(), aggregateId, 1, "CounterAggregateRoot", EventHeader.create("CounterAggregateRoot"),value)


}
```
#### Define a DomainEvent Repository
 A domain repository is created extending the `IAggregateRepository` interface.
The interface contains basically two main functions to implement:
- `fun getById(aggregateId: IIdentity):TAggregate?` used to retrieve the aggregate  
- `fun save(aggregate: TAggregate, commitID: UUID)` used to persiste the aggregate

The interface `IAggregateRepository` allow you to implement any type of repository.
The abstract class `EventStoreRepository` implement some logic to manage the repository as an event store to implement the event sourcing pattern.

Below the implementation of an in memory eventStore repository. A different implementation that use [EventStoreDB](https://www.eventstore.com/eventstoredb) is [kcqrs-EventStoreDB](https://github.com/abaddon/kcqrs-EventStoreDB) 


```kotlin
class InMemoryEventStoreRepository<TAggregate : IAggregate>(
  /** It's the root of the stream. Each aggregate has its dedicated stream. */
  private val _streamNameRoot: String,
  /** It's the function used to create an empty aggregate. It's used during the aggregate rehydration  */
  private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate
) : EventStoreRepository<TAggregate>() {

  /** In memory storage, a map with all aggregate. The key is the stream name and the value a list of DomainEvent  */
  private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()
  /** list of the projection handler subscribed to the event store.  */
  private val projectionHandlers = mutableListOf<IProjectionHandler<*>>()

  override val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
  
  /** it's the logic to create the stream name  */
  override fun aggregateIdStreamName(aggregateId: IIdentity): String = _streamNameRoot

  /**
   * This method should be used only for testing purpose.
   * It allows saving events directly to the Events store without using the aggregate
   */
  fun addEventsToStorage(aggregateId: IIdentity, events: List<IDomainEvent>) {
    persist(aggregateIdStreamName(aggregateId), events, mapOf(), 0)
  }
  
  /**
   * This method should be used only for testing purpose.
   * It allows getting events directly from the Events store
   */
  fun loadEventsFromStorage(aggregateId: IIdentity): List<IDomainEvent> =
    load(aggregateIdStreamName(aggregateId))

  /** 
   * Persist method receive the list of events uncommitted and contain the logic to save the event on the in memory storage.
   * If you want to change the place where store the events you have to change the persist method 
   */
  override fun persist(
    streamName: String,
    uncommittedEvents: List<IDomainEvent>,
    header: Map<String, String>,
    currentVersion: Long
  ) {
    val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
    currentEvents.addAll(uncommittedEvents.toMutableList())
    storage[streamName] = currentEvents
  }

  /**
   *  Load method return the list of events available in the storage related to as specific aggregate.
   */
  override fun load(streamName: String, startFrom: Long): List<IDomainEvent> =
    storage.getOrDefault(streamName, listOf())

  /**
   * The Subscribe method is used to subscribe a projectionHandler allowing it to receive the events published that could be used to update the projections/views
   */
  override fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
    projectionHandlers.add(projectionHandler)
  }

  override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

  override fun publish(events: List<IDomainEvent>) {
    projectionHandlers.forEach{projectionHandlers -> projectionHandlers.onEvents(events)}
  }
}
```

#### Define a Projection

A projection is like a SQL view. Often the data that we have to publish/show following a data structure completely different from our aggregate structure. As consequence, we should implement complex query to reorganise the data in the format that we need.
Projections or Views help to reduce this complexity. A projection is a like a SQL view, where we include only the data that we need to publish. The projection is populated by events generated by the aggregate.
Each view contain internally the business logic that explain how update itself everytime an event come. Each Projection should have a clear and defined purpose. It helps to maintain the logic lean and clean.

In the example the scope of the projection is count each type of event received.
Each projection has to implement the interface `IProjection`.

```kotlin
data class EventTypesCounterProjection(
    override val key: EventTypesCounterProjectionKey, // key of the projection
    val numIncreasedEvent: Int, // num of IncreasedEvent received
    val numDecreaseEvent: Int,  // num of DecreaseEvent received
) : IProjection {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

  /**
   * The method applyEvent, is triggered every time a new event is persisted on the repository.
   * The projection has to identify the event type and change the projection following the business rules 
   */
    override fun applyEvent(event: IDomainEvent): IProjection {
        log.info("applying event with messageId: ${event.messageId}")
        return when (event) {
            is CounterIncreasedEvent -> copy(numIncreasedEvent = this.numIncreasedEvent + 1)
            is CounterDecreaseEvent -> copy(numDecreaseEvent = this.numDecreaseEvent + 1)
            else -> this
        }
    }
}
```

#### Define a Projection Repository

A projection repository is used to persist projections, avoiding having to generate them from the beginning to each new event.
This class has to implement the interface IProjectionRepository.

In the example the repository is in memory

```kotlin
class InMemoryProjectionRepository<TProjection : IProjection>(
  private val _emptyProjection: (key: IProjectionKey)-> TProjection  //function used to create an empty projection if it doesn't exist yet 
) : IProjectionRepository<TProjection> {
    private val inMemoryStorage = mutableMapOf<IProjectionKey, TProjection>()
    var offsetStorage: Long = 0


    override suspend fun getByKey(key: IProjectionKey): TProjection? {
        return inMemoryStorage[key]
    }

    override suspend fun save(projection: TProjection, offset: Long) {
        inMemoryStorage[projection.key] = projection
        offsetStorage = if (offset > offsetStorage) offset else offsetStorage
    }

    override fun emptyProjection(key: IProjectionKey): TProjection =_emptyProjection(key)

}
```

#### Define a Projection Handler
The Projection Handler's purpose is to receive the events published by the EventStore repository, send them the projection and persist the updated projection. 
If the projection doesn't exist, it will be created and then the event is applied to it.

```kotlin
interface IProjectionHandler<TProjection:IProjection> {
    val log: Logger
    val repository: IProjectionRepository<TProjection>

    val projectionKey: IProjectionKey


    @Suppress("UNCHECKED_CAST")
    fun onEvent(event: IDomainEvent) {
        runBlocking {
            try {
                val updatedProjection = (repository.getByKey(projectionKey)
                    ?: repository.emptyProjection(projectionKey)).applyEvent(event) as TProjection
                repository.save(updatedProjection, 0)
            }catch(ex : Exception){
                log.error("Event not applied",ex)
            }
        }
    }

    fun onEvents(events: List<IDomainEvent>) {
        events.forEach{ domainEvent ->
            onEvent(domainEvent)
        }
    }

}
```

