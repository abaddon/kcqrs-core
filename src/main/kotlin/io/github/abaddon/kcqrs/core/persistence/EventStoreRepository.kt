package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import io.github.abaddon.kcqrs.core.helpers.LoggerFactory.log
import io.github.abaddon.kcqrs.core.helpers.flatMap
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import kotlinx.coroutines.withContext
import java.security.InvalidParameterException
import java.time.Instant
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class EventStoreRepository<TAggregate : IAggregate>(
    protected val coroutineContext: CoroutineContext
) : IAggregateRepository<TAggregate> {

    companion object {
        const val COMMIT_ID_HEADER = "CommitId"
        const val COMMIT_DATE_HEADER = "CommitDate"
        const val AGGREGATE_TYPE_HEADER = "AggregateTypeName"
    }

    abstract fun aggregateIdStreamName(aggregateId: IIdentity): String

    protected abstract suspend fun load(streamName: String, startFrom: Long = 0): Result<List<IDomainEvent>>
    protected abstract suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit>

    protected open suspend fun publish(events: List<IDomainEvent>): Result<Unit> =
        Result.success(Unit)


    override suspend fun getById(aggregateId: IIdentity): Result<TAggregate> =
        withContext(coroutineContext) {
            getById(aggregateId, Long.MAX_VALUE)
        }

    override suspend fun getById(aggregateId: IIdentity, version: Long): Result<TAggregate> =
        withContext(coroutineContext) {
            val emptyAggregate = emptyAggregate(aggregateId)
            runCatching {
                check(version > 0) { throw InvalidParameterException("Cannot get version <= 0. Current value: $version") }
            }.flatMap {
                log.debug("Loading aggregate with id: ${aggregateId.valueAsString()} and version: $version")
                load(aggregateIdStreamName(aggregateId))
            }.flatMap { domainEvents ->
                log.debug("Loaded ${domainEvents.size} events for aggregate with id: ${aggregateId.valueAsString()}")
                hydratedAggregate(emptyAggregate, version, domainEvents)
            }.flatMap { hydratedAggregate ->
                log.debug("Hydrated aggregate with id: ${aggregateId.valueAsString()} and version: ${hydratedAggregate.version}")
                when (hydratedAggregate.version != version) {
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


    private fun hydratedAggregate(
        initial: TAggregate,
        currentVersion: Long,
        domainEvents: List<IDomainEvent>
    ): Result<TAggregate> = runCatching {
        domainEvents.foldEvents(initial, currentVersion)
    }


    override suspend fun save(
        aggregate: TAggregate,
        commitID: UUID,
        updateHeaders: () -> Map<String, String>
    ): Result<TAggregate> = withContext(coroutineContext) {
        val header: Map<String, String> = buildHeaders(aggregate, commitID, updateHeaders())
        val uncommittedEvents: List<IDomainEvent> = aggregate.uncommittedEvents()
        val currentVersion = aggregate.version - uncommittedEvents.size
        log.info("Saving aggregate ${aggregate.id} version: ${aggregate.version}, uncommittedEvents.size: ${uncommittedEvents.size}, currentVersion: $currentVersion")

        persist(aggregateIdStreamName(aggregate.id), uncommittedEvents, header, currentVersion)
            .flatMap {
                log.debug("Persisted ${uncommittedEvents.size} events for aggregate ${aggregate.id.valueAsString()}")
                publish(uncommittedEvents)
            }.flatMap {
                log.debug("Published ${uncommittedEvents.size} events for aggregate ${aggregate.id.valueAsString()}")
                Result.success(aggregate)
            }
    }

    override suspend fun save(aggregate: TAggregate, commitID: UUID) = withContext(coroutineContext) {
        save(aggregate, commitID) {
            mapOf()
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