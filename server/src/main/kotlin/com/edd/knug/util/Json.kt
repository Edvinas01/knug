package com.edd.knug.util

import com.google.gson.Gson
import java.util.*

class Json {

    private val gson = Gson()

    /**
     * Write manually constructed json object to string.
     */
    fun write(builder: JsonBuilder.() -> Unit) = write(JsonBuilder().json(builder))

    /**
     * Write object to json string.
     */
    fun write(obj: Any): String = gson.toJson(obj)

    /**
     * Read json from provided json string.
     */
    inline fun <reified T> read(json: String) = read(T::class.java, json)

    /**
     * Read json from provided json string and cast it to desired type.
     */
    fun <T> read(type: Class<T>, json: String): T = gson.fromJson(json, type)
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