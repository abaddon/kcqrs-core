package io.github.abaddon.kustomCompare

import io.github.abaddon.kustomCompare.config.CompareLogicConfig
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class CompareLogic(
    val config: CompareLogicConfig
) {
    constructor():this(CompareLogicConfig())

    inline fun <reified T: Any>compare(element1: T, element2: T): CompareResult {

        val testResult =
            element1::class.memberProperties.filter { member -> !config.membersToIgnore.contains(member.name) }
                .associate { member ->
                    val property = member as KProperty1<Any, *>
                    Pair<String, Boolean>(member.name, property.get(element1) == property.get(element2))
                }
        val propertyCheck = testResult.filterValues{ element -> !element }.keys

        return CompareResult(propertyCheck.isEmpty(),propertyCheck)
    }
}