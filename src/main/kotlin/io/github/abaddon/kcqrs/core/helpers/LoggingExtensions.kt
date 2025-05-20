package io.github.abaddon.kcqrs.core.helpers

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.java
import kotlin.reflect.KClass

// Extension property for any class
val <T : Any> T.log: Logger
    get() = LogManager.getLogger(this::class.java)

// Extension property for KClass
val <T : Any> KClass<T>.log: Logger
    get() = LogManager.getLogger(this.java)