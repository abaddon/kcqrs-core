package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IIdentity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EntityTest{
    @Test
    fun `Given an identity when create the aggregate then the aggregate has the same identity`() {
        val identity = DummyIdentity()
        val actualEntity = DummyEntity(identity)
        val expectedIdentity = "1234"

        assertEquals(expectedIdentity, actualEntity.identity().valueAsString())

    }

    private class DummyIdentity: IIdentity {
        override fun valueAsString(): String ="1234"
    }

    private class DummyEntity(identity: IIdentity): Entity(identity)
}