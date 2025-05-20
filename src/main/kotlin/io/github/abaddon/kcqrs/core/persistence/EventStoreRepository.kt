package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.Result
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import io.github.abaddon.kcqrs.core.helpers.log
import java.security.InvalidParameterException
import java.time.Instant
import java.util.*

abstract class EventStoreRepository<TAggregate : IAggregate> : IAggregateRepository<TAggregate> {

    companion object {
        const val COMMIT_ID_HEADER = "CommitId"
        const val COMMIT_DATE_HEADER = "CommitDate"
        const val AGGREGATE_TYPE_HEADER = "AggregateTypeName"
    }

    abstract fun aggregateIdStreamName(aggregateId: IIdentity): String

    protected abstract fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    )

    protected abstract fun publish(events: List<IDomainEvent>)

    protected abstract fun load(streamName: String, startFrom: Long = 0): List<IDomainEvent>

    override suspend fun getById(aggregateId: IIdentity): Result<Exception, TAggregate> {
        return getById(aggregateId, Long.MAX_VALUE)
    }

    override suspend fun getById(aggregateId: IIdentity, version: Long): Result<Exception, TAggregate> {
        return try {
            check(version > 0) { throw InvalidParameterException("Cannot get version <= 0. Current value: $version") }

            val emptyAggregate = emptyAggregate(aggregateId)

            val hydratedAggregate = load(aggregateIdStreamName(aggregateId)).foldEvents(emptyAggregate, version)

            when (hydratedAggregate.version != version && version <= Long.MAX_VALUE) {
                true -> Result.Valid(hydratedAggregate)
                false -> Result.Invalid(
                    AggregateVersionException(
                        aggregateId,
                        hydratedAggregate::javaClass.name,
                        hydratedAggregate.version,
                        version
                    )
                )
            }
        } catch (e: InvalidParameterException) {
            Result.Invalid(e)
        } catch (e: Exception) {
            Result.Invalid(e)
        }

    }

    override suspend fun save(
        aggregate: TAggregate,
        commitID: UUID,
        updateHeaders: () -> Map<String, String>
    ): Result<Exception, TAggregate> {
        val header: Map<String, String> = buildHeaders(aggregate, commitID, updateHeaders())
        val uncommittedEvents: List<IDomainEvent> = aggregate.uncommittedEvents()
        val currentVersion = aggregate.version - uncommittedEvents.size
        log.info("aggregate.version: ${aggregate.version}, uncommittedEvents.size: ${uncommittedEvents.size}, currentVersion: $currentVersion")
        persist(aggregateIdStreamName(aggregate.id), uncommittedEvents, header, currentVersion)
        publish(uncommittedEvents)

        return Result.Valid(aggregate)
    }

    override suspend fun save(aggregate: TAggregate, commitID: UUID): Result<Exception, TAggregate> =
        save(aggregate, commitID) {
            mapOf()
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