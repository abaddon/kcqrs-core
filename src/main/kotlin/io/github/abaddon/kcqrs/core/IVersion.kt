package io.github.abaddon.kcqrs.core

interface IVersion {
    val value: Long;

    fun incrementVersion(): IVersion
}