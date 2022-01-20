package io.github.abaddon.kcqrs.core.helpers

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent

//val mapper = ObjectMapper().registerModule(
//    KotlinModule.Builder()
//        .withReflectionCacheSize(512)
//        .configure(KotlinFeature.NullToEmptyCollection, false)
//        .configure(KotlinFeature.NullToEmptyMap, false)
//        .configure(KotlinFeature.NullIsSameAsDefault, false)
//        .configure(KotlinFeature.SingletonSupport,false)
//        .configure(KotlinFeature.StrictNullChecks, false)
//        .build()
//)

fun <TAggregate : IAggregate> Iterable<IEvent>.foldEvents(initial: TAggregate): TAggregate {
    var accumulator = initial
    for (element in this) {
        accumulator = accumulator.applyEvent(element) as TAggregate
    }
    return accumulator
}

//inline fun <reified T: DomainEvent>T.serialise(): String = //mapper.writeValueAsString(this)
//    Json.encodeToString(this)
//    //val eventJson = mapper.writeValueAsString(this)


//inline fun <reified T: DomainEvent>T.deserialize(domainEventJson: String): T = Json.decodeFromString<T>(domainEventJson)
