package io.github.abaddon.kcqrs.eventstores.eventstoredb

import com.eventstore.dbclient.EventData
import com.eventstore.dbclient.ResolvedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.abaddon.kcqrs.core.domain.messages.events.DomainEvent

val mapper = ObjectMapper().registerModule(
    KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, false)
        .configure(KotlinFeature.NullToEmptyMap, false)
        .configure(KotlinFeature.NullIsSameAsDefault, false)
        .configure(KotlinFeature.SingletonSupport,false)
        .configure(KotlinFeature.StrictNullChecks, false)
        .build()
)

fun Iterable<ResolvedEvent>.toDomainEvents(): Iterable<DomainEvent<*>> {
    return this.map {  resolvedEvent -> resolvedEvent.originalEvent.toDomainEvent()  }
}

fun com.eventstore.dbclient.RecordedEvent.toDomainEvent(): DomainEvent<*>{
    val eventTypeName = this.eventType

    val eventClass = Class.forName(eventTypeName)

    val eventDataJson: String=this.eventData.decodeToString()
    val eventMetaJson: String=this.userMetadata.decodeToString() //TODO headers not managed

    return mapper.readValue(eventDataJson,eventClass) as DomainEvent<*>
}

inline fun <reified T: DomainEvent<*>>T.toEventData(header: Map<String,String>): EventData {
    val eventId = this.messageId;
    val eventType = this::class.qualifiedName!!

    val eventJson = mapper.writeValueAsString(this)
    val headerJson = mapper.writeValueAsString(header)
    return EventData(eventId,eventType,"application/json",eventJson.encodeToByteArray(), headerJson.encodeToByteArray())
}