package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import io.github.abaddon.kcqrs.core.helpers.KcqrsLoggerFactory.log
import io.github.abaddon.kcqrs.core.helpers.flatMap
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import kotlinx.coroutines.flow.Flow
import java.security.InvalidParameterException
import java.time.Instant
import java.util.UUID

abstract class EventStoreRepository<TAggregate : IAggregate>(
) : IAggregateRepository<TAggregate> {

    companion object {
        const val COMMIT_ID_HEADER = "CommitId"
        const val COMMIT_DATE_HEADER = "CommitDate"
        const val AGGREGATE_TYPE_HEADER = "AggregateTypeName"
    }

    abstract fun aggregateIdStreamName(aggregateId: IIdentity): String

    protected abstract suspend fun loadEvents(streamName: String, startFrom: Long = 0): Result<Flow<IDomainEvent>>
    protected abstract suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit>

    protected open suspend fun publish(events: List<IDomainEvent>): Result<Unit> =
        Result.success(Unit)


    override suspend fun getById(aggregateId: IIdentity): Result<TAggregate> =
        getById(aggregateId, Long.MAX_VALUE)


    override suspend fun getById(aggregateId: IIdentity, version: Long): Result<TAggregate> {
        val emptyAggregate = emptyAggregate(aggregateId)
        return runCatching {
            check(version > 0) { throw InvalidParameterException("Cannot get version <= 0. Current value: $version") }
        }.flatMap {
            log.debug("Loading aggregate with id: ${aggregateId.valueAsString()}")
            loadEvents(aggregateIdStreamName(aggregateId))
        }.flatMap { domainEvents ->
            log.debug("Events loaded for aggregate with id: ${aggregateId.valueAsString()}")
            hydratedAggregate(emptyAggregate, version, domainEvents)
        }.flatMap { hydratedAggregate ->
            log.debug("Hydrated aggregate with id: ${aggregateId.valueAsString()} and version: ${hydratedAggregate.version}")
            when (version == Long.MAX_VALUE || hydratedAggregate.version == version) {
                true -> Result.success(hydratedAggregate)
                false -> Result.failure(
                    AggregateVersionException(
                        aggregateId,
                        hydratedAggregate::javaClass.name,
                        hydratedAggregate.version,
                        version
                    )
                )
            }
        }
    }


    private suspend fun hydratedAggregate(
        initial: TAggregate,
        currentVersion: Long,
        domainEvents: Flow<IDomainEvent>
    ): Result<TAggregate> = runCatching {
        domainEvents.foldEvents(initial, currentVersion)
    }

    override suspend fun save(aggregate: TAggregate, commitID: UUID): Result<TAggregate> =
        save(aggregate, commitID) {
            mapOf()
        }

    override suspend fun save(
        aggregate: TAggregate,
        commitID: UUID,
        updateHeaders: () -> Map<String, String>
    ): Result<TAggregate> {
        val header: Map<String, String> = buildHeaders(aggregate, commitID, updateHeaders())
        val uncommittedEvents: List<IDomainEvent> = aggregate.uncommittedEvents()
        val currentVersion = aggregate.version - uncommittedEvents.size
        log.info("Saving aggregate ${aggregate.id} version: ${aggregate.version}, uncommittedEvents.size: ${uncommittedEvents.size}, currentVersion: $currentVersion")

        return persist(aggregateIdStreamName(aggregate.id), uncommittedEvents, header, currentVersion)
            .flatMap {
                log.debug("Persisted ${uncommittedEvents.size} events for aggregate ${aggregate.id.valueAsString()}")
                publish(uncommittedEvents)
            }.flatMap {
                log.debug("Published ${uncommittedEvents.size} events for aggregate ${aggregate.id.valueAsString()}")
                Result.success(aggregate)
            }
    }

    private fun buildHeaders(
        aggregate: TAggregate,
        commitID: UUID,
        customHeader: Map<String, String>
    ): Map<String, String> {
        return customHeader + mapOf(
            Pair(COMMIT_ID_HEADER, commitID.toString()),
            Pair(COMMIT_DATE_HEADER, Instant.now().toString()),
            Pair(AGGREGATE_TYPE_HEADER, aggregate::class.simpleName.orEmpty()),
        )
    }

}