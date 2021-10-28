package io.github.abaddon.kcqrs.core

import java.util.*

interface IIdentity {
    fun value(): UUID
}