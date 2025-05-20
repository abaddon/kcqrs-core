package io.github.abaddon.kcqrs.core.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

// Extension property for any class
val <T : Any> T.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)

// Extension property for KClass
val <T : Any> KClass<T>.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)