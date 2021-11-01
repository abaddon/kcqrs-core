package io.github.abaddon.kcqrs.core.exceptions

import io.github.abaddon.kcqrs.core.IIdentity
import kotlin.reflect.KClass

class AggregateVersionException( aggregateId: IIdentity, kClass: KClass<*>, aggregateVersion: Long, requestedVersion: Long ) : Exception("Requested version $requestedVersion of aggregate $aggregateId (type ${kClass.simpleName}) - aggregate version is $aggregateVersion") {

}