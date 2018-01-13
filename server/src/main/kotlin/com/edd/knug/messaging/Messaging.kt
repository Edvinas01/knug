package com.edd.knug.messaging

import java.util.concurrent.ConcurrentLinkedQueue

class Messaging {

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<Event>>>()

    // Events might arrive from main and web-socket threads, so gotta sync it.
    private val events = ConcurrentLinkedQueue<Event>()

    fun <T : Event> listen(type: Class<T>, listener: Listener<T>) {
        var existing = listeners[type]
        if (existing == null) {
            existing = mutableListOf()
            listeners[type] = existing
        }

        @Suppress("UNCHECKED_CAST")
        existing.add(listener as Listener<Event>)
    }

    inline fun <reified T : Event> listen(crossinline listener: (T) -> Unit) {
        listen(object : Listener<T> {
            override fun listen(event: T) {
                listener(event)
            }
        })
    }

    inline fun <reified T : Event> listen(listener: Listener<T>) {
        listen(T::class.java, listener)
    }

    fun process() {
        var event: Event?
        do {
            event = events.poll()?.also { e ->
                listeners[e.javaClass]?.forEach { l ->
                    l.listen(e)
                }
            }
        } while (event != null)
    }

    fun <T : Event> send(event: T) {
        events.offer(event)
    }
}