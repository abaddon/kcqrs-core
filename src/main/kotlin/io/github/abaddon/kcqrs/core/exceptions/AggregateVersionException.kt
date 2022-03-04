package io.github.abaddon.kcqrs.core.exceptions

import io.github.abaddon.kcqrs.core.IIdentity

class AggregateVersionException(
    aggregateId: IIdentity,
    className: String,
    aggregateVersion: Long,
    requestedVersion: Long
) : Exception("Requested version $requestedVersion of aggregate $aggregateId (type ${className}) - aggregate version is $aggregateVersion") {

}