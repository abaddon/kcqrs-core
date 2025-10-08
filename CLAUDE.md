## Scope

**kcqrs-core** is a Kotlin CQRS (Command Query Responsibility Segregation) library.
It provides the foundational components for implementing event-sourced, CQRS-based applications in Kotlin.

### Target Use Cases
- Event-sourced systems where aggregates are reconstructed from domain events
- Applications requiring separation of read (query) and write (command) models
- Systems needing strong audit trails and event history
- Domain-driven design (DDD) implementations with complex business logic

### Key Architectural Patterns
- **Event Sourcing**: Aggregates are persisted as streams of domain events, not current state
- **CQRS**: Separate command handlers (write side) from projection handlers (read side)
- **Convention-based Event Routing**: Aggregate applies events using convention-based method dispatch (apply functions)
- **Immutability**: Aggregates are immutable data classes; operations return new instances

## Main Components

### Core Domain Entities

#### 1. **IIdentity** (`IIdentity.kt`)
- Unique identifier for aggregates
- Must provide `valueAsString()` for serialization
- Typically wraps UUID

#### 2. **Entity** (`Entity.kt`)
- Base domain entity with identity
- Foundation for value objects and entities in DDD

#### 3. **IAggregate / AggregateRoot** (`IAggregate.kt`, `AggregateRoot.kt`)
- **Core concept**: Represents the aggregate root in DDD
- Contains:
  - `id: IIdentity` - unique aggregate identifier
  - `version: Long` - version for optimistic concurrency control
  - `uncommittedEvents` - events raised but not yet persisted
- **Key behavior**:
  - Validates business rules before raising events
  - Raises domain events via `raiseEvent()`
  - Events are applied via convention-based routing (functions named `apply(event)`)
  - Maintains immutability - operations return new instances
- **Event Router**: Uses `ConventionEventRouter` to dispatch events to appropriate `apply()` functions

### Commands (Write Side)

#### 4. **ICommand / Command** (`Command.kt`, `ICommand.kt`)
- Represents intent to perform an operation on an aggregate
- Contains:
  - `aggregateID` - target aggregate identifier
  - `messageId` - unique command identifier
  - `commandHeaders` - metadata (user, correlation ID, etc.)
- Must implement `execute(currentAggregate?)` with business logic
- Returns new aggregate instance after execution

#### 5. **IAggregateCommandHandler** (`IAggregateCommandHandler.kt`)
- Processes commands and orchestrates aggregate lifecycle
- **SimpleAggregateCommandHandler**: Standard implementation that:
  1. Retrieves aggregate from repository (rehydrates from events)
  2. Executes command on aggregate
  3. Saves resulting uncommitted events to repository
- Handles correlation and transaction management

### Queries (Read Side)

#### 5a. **IQuery / Query** (`Query.kt`, `IQuery.kt`)
- Represents a request for data from the read side (projections)
- Contains:
  - `messageId` - unique query identifier
  - `queryHeaders` - metadata (user, correlation ID, etc.)
  - `queryDate` - timestamp when query was created
- Must implement `suspend fun execute(): Result<TResult>` with query logic
- Returns typed result (projection data, DTO, or any transformed data)
- Query receives required repositories via constructor injection
- Can aggregate multiple projections or transform projection data

#### 5b. **IQueryHandler** (`IQueryHandler.kt`)
- Processes queries and returns requested data
- **SimpleQueryHandler**: Standard implementation that:
  1. Executes query by calling `query.execute()`
  2. Logs query execution for observability
  3. Returns result to caller
- Simpler than CommandHandler - no state changes or persistence
- Supports header enrichment for tracing

### Events (Source of Truth)

#### 6. **IDomainEvent** (`IDomainEvent.kt`)
- Immutable record of something that happened in the domain
- Contains:
  - `messageId` - unique event identifier
  - `aggregateId` - aggregate that raised the event
  - `aggregateType` - aggregate class name
  - `version` - event schema version
  - `header: EventHeader` - metadata (timestamp, user, etc.)
- Represents the source of truth for aggregate state
- Used to rehydrate aggregates (event sourcing)

### Repositories (Persistence)

#### 7. **IAggregateRepository** (`IAggregateRepository.kt`)
- Interface for aggregate persistence
- Key methods:
  - `getById(aggregateId)` - retrieves and rehydrates aggregate from events
  - `save(aggregate, commitID)` - persists uncommitted events

#### 8. **EventStoreRepository** (`EventStoreRepository.kt`)
- Abstract base class for event-sourced repositories
- Implements event sourcing pattern:
  - Loads events from stream
  - Rehydrates aggregate by folding events through `applyEvent()`
  - Persists uncommitted events to stream
  - Publishes events to projection handlers
- Handles optimistic concurrency via version checking
- Includes `InMemoryEventStoreRepository` for testing

### Projections (Read Side)

#### 9. **IProjection** (`IProjection.kt`)
- Read model / view optimized for queries
- Contains:
  - `key: IProjectionKey` - unique projection identifier
  - `lastProcessedEvent` - tracking for event position
  - `lastUpdated` - timestamp of last update
- Implements `applyEvent(event)` to update itself based on events
- Represents denormalized views for efficient querying
- Typically follows specific use cases (e.g., reporting, dashboards)

#### 10. **IProjectionRepository** (`IProjectionRepository.kt`)
- Interface for projection persistence
- Key methods:
  - `getByKey(key)` - retrieves projection by key
  - `save(projection, offset)` - persists updated projection
  - `emptyProjection(key)` - creates new projection instance
- Includes `InMemoryProjectionRepository` for testing

#### 11. **IProjectionHandler** (`IProjectionHandler.kt`, `ProjectionHandler.kt`)
- Subscribes to domain events from event store
- Updates projections based on events received
- Process:
  1. Receives domain event(s) from repository
  2. Retrieves or creates projection
  3. Applies event to projection
  4. Persists updated projection
- Enables eventual consistency on read side

### Supporting Components

#### 12. **EventHeader / CommandHeaders / QueryHeaders** (`EventHeader.kt`, `CommandHeaders.kt`, `QueryHeaders.kt`)
- Metadata for events, commands, and queries
- Contains correlation IDs, user context, timestamps
- Enables tracing and auditing across write and read operations

#### 13. **Result Helpers** (`ResultHelpers.kt`)
- Functional error handling using `Result<T>` type
- Provides `flatMap` and other monadic operations
- Avoids exception-based control flow

## Architecture Flow

### Command Flow (Write Side)
1. Client sends **Command** to **CommandHandler**
2. CommandHandler retrieves **Aggregate** from **AggregateRepository** (rehydrated from events)
3. Command executes business logic on Aggregate
4. Aggregate validates and raises **DomainEvents** (uncommitted)
5. CommandHandler saves Aggregate (persists uncommitted events to EventStore)
6. EventStore publishes events to **ProjectionHandlers**

### Query Flow (Read Side)

#### Projection Update Flow (Event-Driven)
1. **ProjectionHandler** receives **DomainEvents** from EventStore
2. ProjectionHandler retrieves **Projection** from **ProjectionRepository**
3. Events applied to Projection via `applyEvent()`
4. Updated Projection persisted to ProjectionRepository

#### Query Execution Flow (Client-Driven)
1. Client sends **Query** to **QueryHandler**
2. QueryHandler delegates to `query.execute()`
3. Query retrieves data from **ProjectionRepository** (or multiple repositories)
4. Query transforms/aggregates projection data as needed
5. Query returns typed result (projection data, DTO, or computed result)
6. QueryHandler returns result to client (fast, denormalized reads)

## Design Principles

- **Event Sourcing**: State is derived from events, not stored directly
- **Immutability**: Aggregates and events are immutable
- **Eventual Consistency**: Read side (projections) updated asynchronously
- **Separation of Concerns**: Commands/writes separated from queries/reads
- **Domain-Driven Design**: Aggregates encapsulate business logic and invariants
- **Convention over Configuration**: Event routing uses naming conventions

## Prompts
- always think hard and produce a plan before execute any change
- Always test any change