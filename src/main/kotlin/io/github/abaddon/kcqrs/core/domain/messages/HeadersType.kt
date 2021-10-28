package io.github.abaddon.kcqrs.core.domain.messages

enum class HeadersType(val label: String){
    WHEN("createdAt"),
    WHO("generatedBy"),
    EVENT_TYPE("eventType"),
    CORRELATION_ID("correlation_id")
}