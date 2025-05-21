package io.github.abaddon.kcqrs.core.persistence

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.AggregateVersionException
import io.github.abaddon.kcqrs.core.helpers.foldEvents
import io.github.abaddon.kcqrs.core.helpers.log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.security.InvalidParameterException
import java.time.Instant
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class EventStoreRepository<TAggregate : IAggregate>(
    dispatcher: CoroutineDispatcher
) : IAggregateRepository<TAggregate> {

    companion object {
        const val COMMIT_ID_HEADER = "CommitId"
        const val COMMIT_DATE_HEADER = "CommitDate"
        const val AGGREGATE_TYPE_HEADER = "AggregateTypeName"
    }

    // Create a dedicated coroutine scope for this repository
    private val job = SupervisorJob()
    val coroutineContext: CoroutineContext = job + dispatcher

    abstract fun aggregateIdStreamName(aggregateId: IIdentity): String

    protected abstract suspend fun load(streamName: String, startFrom: Long = 0): List<IDomainEvent>
    protected abstract suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit>

    protected open suspend fun publish(persistResult: Result<Unit>, events: List<IDomainEvent>): Result<Unit> =
        Result.success(Unit)


    override suspend fun getById(aggregateId: IIdentity): Result<TAggregate> =
        withContext(coroutineContext) {
            getById(aggregateId, Long.MAX_VALUE)
        }

    override suspend fun getById(aggregateId: IIdentity, version: Long): Result<TAggregate> =
        withContext(coroutineContext) {
            try {
                check(version > 0) { throw InvalidParameterException("Cannot get version <= 0. Current value: $version") }

                val emptyAggregate = emptyAggregate(aggregateId)

                val hydratedAggregate = load(aggregateIdStreamName(aggregateId)).foldEvents(emptyAggregate, version)

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
            } catch (e: InvalidParameterException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        }

    override suspend fun save(
        aggregate: TAggregate,
        commitID: UUID,
        updateHeaders: () -> Map<String, String>
    ): Result<Unit> = withContext(coroutineContext) {
        val header: Map<String, String> = buildHeaders(aggregate, commitID, updateHeaders())
        val uncommittedEvents: List<IDomainEvent> = aggregate.uncommittedEvents()
        val currentVersion = aggregate.version - uncommittedEvents.size
        log.info("aggregate.version: ${aggregate.version}, uncommittedEvents.size: ${uncommittedEvents.size}, currentVersion: $currentVersion")

        val persistResult = persist(aggregateIdStreamName(aggregate.id), uncommittedEvents, header, currentVersion)
        publish(persistResult, uncommittedEvents)
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

    // Method to release resources when a repository is no longer needed
    fun cleanup() {
        job.cancel()
    }

}