package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IIdentity

abstract class Entity(identity: IIdentity){
    private val identity: IIdentity

    init {
        this.identity = identity
    }

    fun identity(): IIdentity = this.identity

}