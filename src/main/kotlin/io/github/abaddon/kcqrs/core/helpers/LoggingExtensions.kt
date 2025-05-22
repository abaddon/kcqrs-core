package io.github.abaddon.kcqrs.core.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

// Extension property for any class
val <T : Any> T.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)

// Extension property for KClass
val <T : Any> KClass<T>.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)

object LoggerFactory {
    private val loggerCache = ConcurrentHashMap<String, Logger>()

    val <T : Any> T.log: Logger
        get() = loggerCache.computeIfAbsent(this::class.qualifiedName ?: "missing_class") {
            println("logger created for ${this::class.qualifiedName}")
            LoggerFactory.getLogger(this::class.qualifiedName)
        }

}