package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import org.slf4j.Logger
import java.security.InvalidParameterException
import java.time.Instant
import java.util.*

abstract class EventStoreRepository<TAggregate : IAggregate> : IAggregateRepository<TAggregate> {

    companion object {
        const val COMMIT_ID_HEADER = "CommitId"
        const val COMMIT_DATE_HEADER = "CommitDate"
        const val AGGREGATE_TYPE_HEADER = "AggregateTypeName"
    }

    abstract val log: Logger
    abstract fun aggregateIdStreamName(aggregateId: IIdentity): String

    protected abstract fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    )

    protected abstract fun publish(events: List<IDomainEvent>)

    protected abstract fun load(streamName: String, startFrom: Long = 0): List<IDomainEvent>

    override suspend fun getById(aggregateId: IIdentity): TAggregate {
        return getById(aggregateId, Long.MAX_VALUE)
    }

    override suspend fun getById(aggregateId: IIdentity, version: Long): TAggregate {
        check(version > 0) { throw InvalidParameterException("Cannot get version <= 0. Current value: $version") }

        val emptyAggregate = emptyAggregate(aggregateId)

        val hydratedAggregate = load(aggregateIdStreamName(aggregateId)).foldEvents(emptyAggregate, version)

        check(hydratedAggregate.version != version && version <= Long.MAX_VALUE) {
            throw AggregateVersionException(
                aggregateId,
                hydratedAggregate::javaClass.name,
                hydratedAggregate.version,
                version
            )
        }

        return hydratedAggregate
    }

    override suspend fun save(aggregate: TAggregate, commitID: UUID, updateHeaders: () -> Map<String, String>) {
        val header: Map<String, String> = buildHeaders(aggregate, commitID, updateHeaders())
        val uncommittedEvents: List<IDomainEvent> = aggregate.uncommittedEvents()
        val currentVersion = aggregate.version - uncommittedEvents.size
        log.info("aggregate.version: ${aggregate.version}, uncommittedEvents.size: ${uncommittedEvents.size}, currentVersion: $currentVersion")
        persist(aggregateIdStreamName(aggregate.id), uncommittedEvents, header, currentVersion)
        publish(uncommittedEvents)
    }

    override suspend fun save(aggregate: TAggregate, commitID: UUID) {
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