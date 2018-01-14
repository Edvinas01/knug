package com.edd.knug.util

import java.util.*

object Json {

    /**
     * Create a json like hash map.
     */
    fun obj(builder: JsonBuilder.() -> Unit) = JsonBuilder().json(builder)
}

class JsonBuilder {

    private val deque: Deque<MutableMap<String, Any>> = ArrayDeque()

    /**
     * Build a "json" like hash-map.
     */
    fun json(build: JsonBuilder.() -> Unit): Map<String, Any> {
        deque.push(mutableMapOf())
        build()
        return deque.pop()
    }

    /**
     * Add a key mapping to a value to the json map.
     */
    infix fun <T : Any> String.to(value: T) = deque.peek().put(this, value)
}