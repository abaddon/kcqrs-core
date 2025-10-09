package io.github.abaddon.kcqrs.core.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory as Slf4jLoggerFactory

object KcqrsLoggerFactory {
    val <T : Any> T.log: Logger
        get() = Slf4jLoggerFactory.getLogger(this::class.qualifiedName ?: "missing_class")
}