package io.github.abaddon.kcqrs.core.domain

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IRouteEvents
import io.github.abaddon.kcqrs.core.domain.messages.events.IEvent
import io.github.abaddon.kcqrs.core.helpers.throwHandlerNotFound
import java.lang.reflect.Method
import kotlin.reflect.KClass

class ConventionEventRouter(private var throwOnApplyNotFound: Boolean) : IRouteEvents {

    private val handlers: MutableMap<String, (event: IEvent) -> IAggregate> = mutableMapOf()
    private lateinit var registered: IAggregate;

    constructor() : this(true)

    constructor(aggregate: IAggregate) : this(true) {
        register(aggregate)
    }


    override fun register(klass: KClass<*>, handler: (event: IEvent) -> IAggregate) {
        register(klass.simpleName!!, handler)
    }

    override fun register(aggregate: IAggregate) {
        registered = aggregate
        val applyMethods = aggregate::class.java.declaredMethods
            .filter { method -> method.name == "apply" && method.parameterCount ==1 }
            .map { method -> mapOf(
                Pair("messageType", method.parameters.first().type.simpleName),
                Pair("method", method)
            ) }

        applyMethods.forEach(){applyMethod ->
            val elementType = applyMethod["messageType"] as String
            val method = applyMethod["method"] as Method
            method.isAccessible = true
            val handler: (event: IEvent) -> IAggregate = { e: IEvent -> method.invoke(aggregate,e) as IAggregate }
            register(elementType,handler)
            //handlers[elementType] = handler
        }

    }

    override fun dispatch(eventMessage: IEvent): IAggregate {
        val handler = handlers[eventMessage::class.simpleName]
        var newAggregate: IAggregate = registered;
        if(handler != null){
            newAggregate = handler(eventMessage)
        }else if(throwOnApplyNotFound){
            registered.throwHandlerNotFound(eventMessage)
        }
         return newAggregate;
    }

    private fun register(messageType: String, handler: (event: IEvent) -> IAggregate) {
        handlers[messageType] = handler
    }

}