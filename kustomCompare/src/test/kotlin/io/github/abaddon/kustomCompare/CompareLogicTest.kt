package io.github.abaddon.kustomCompare

import io.github.abaddon.kustomCompare.config.CompareLogicConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

internal class CompareLogicTest{

    @Test
    fun `Given 2 different objects When compare Then they are not the same`() {
        val user1 = UserTest("stefano",Instant.now())
        val user2 = UserTest("massimo",Instant.now())
        val compareLogic = CompareLogic()
        val compareResult = compareLogic.compare(user1,user2)
        val expectedStringResult="Result:\n" +
                "test: false\n" +
                "unmatched properties: \n" +
                "[birthdate, name]"

        assertFalse(compareResult.result())
        assertEquals(2,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    @Test
    fun `Given 2 objects with the same parameters When compare Then they are the same`() {
        val birthdate = Instant.now()
        val user1 = UserTest("stefano",birthdate)
        val user2 = UserTest("stefano",birthdate)
        val compareLogic = CompareLogic()
        val expectedStringResult="Result:\n" +
                "test: true\n" +
                "unmatched properties: \n" +
                "[]"

        val compareResult = compareLogic.compare(user1,user2)
        assertTrue(compareResult.result())
        assertEquals(0,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    @Test
    fun `Given 2 different ClassRoomTest When compare Then they are not the same`() {
        val birthdate = Instant.now()
        val user1 = UserTest("stefano",birthdate)
        val user2 = UserTest("stefano",birthdate)
        val classRoom1 = ClassRoomTest("Y1", listOf(user1,user2))
        val classRoom2 = ClassRoomTest("Y1", listOf(user1,user2,user1))
        val expectedStringResult="Result:\n" +
                "test: false\n" +
                "unmatched properties: \n" +
                "[students]"

        val compareLogic = CompareLogic()
        val compareResult = compareLogic.compare(classRoom1,classRoom2)
        assertFalse(compareResult.result())
        assertEquals(1,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    @Test
    fun `Given 2 ClassRoomTest with different Users When compare Then they are not the same`() {
        val birthdate = Instant.now()
        val user1 = UserTest("stefano",birthdate)
        val user2 = UserTest("stefano",birthdate)
        val user3 = UserTest("massimo",birthdate)
        val classRoom1 = ClassRoomTest("Y1", listOf(user1,user2))
        val classRoom2 = ClassRoomTest("Y1", listOf(user1,user3))
        val expectedStringResult="Result:\n" +
                "test: false\n" +
                "unmatched properties: \n" +
                "[students]"

        val compareLogic = CompareLogic()
        val compareResult = compareLogic.compare(classRoom1,classRoom2)
        assertFalse(compareResult.result())
        assertEquals(1,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    @Test
    fun `Given 2 ClassRoomTest with the same parameters When compare Then they are the same`() {
        val birthdate = Instant.now()
        val user1 = UserTest("stefano",birthdate)
        val user2 = UserTest("stefano",birthdate)
        val classRoom1 = ClassRoomTest("Y1", listOf(user1,user2))
        val classRoom2 = ClassRoomTest("Y1", listOf(user1,user2))

        val expectedStringResult="Result:\n" +
                "test: true\n" +
                "unmatched properties: \n" +
                "[]"

        val compareLogic = CompareLogic()
        val compareResult = compareLogic.compare(classRoom1,classRoom2)

        assertTrue(compareResult.result())
        assertEquals(0,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    @Test
    fun `Given 2 ClassRoomTest with different students When compare excluding students Then they are the same`() {
        val birthdate = Instant.now()
        val user1 = UserTest("stefano",birthdate)
        val user2 = UserTest("stefano",birthdate)
        val user3 = UserTest("massimo",birthdate)
        val classRoom1 = ClassRoomTest("Y1", listOf(user1,user2))
        val classRoom2 = ClassRoomTest("Y1", listOf(user1,user3))

        val expectedStringResult="Result:\n" +
                "test: true\n" +
                "unmatched properties: \n" +
                "[]"

        val compareLogic = CompareLogic(CompareLogicConfig().addMemberToIgnore("students"))
        val compareResult = compareLogic.compare(classRoom1,classRoom2)
        assertTrue(compareResult.result())
        assertEquals(0,compareResult.unMatchedProperties().size)
        assertEquals(expectedStringResult,compareResult.toString())
    }

    data class UserTest(
        val name: String,
        val birthdate: Instant
    )

    data class ClassRoomTest(
        val name: String,
        val students: List<UserTest>
    )
}