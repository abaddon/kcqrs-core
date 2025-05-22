package io.github.abaddon.kcqrs.core.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object LoggerFactory {
    private val loggerCache = ConcurrentHashMap<String, Logger>()

    val <T : Any> T.log: Logger
        get() = loggerCache.computeIfAbsent(this::class.qualifiedName ?: "missing_class") {
            println("logger created for ${this::class.qualifiedName}")
            LoggerFactory.getLogger(this::class.qualifiedName)
        }

}