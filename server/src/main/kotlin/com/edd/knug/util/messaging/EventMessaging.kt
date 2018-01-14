package com.edd.knug.util.messaging

import java.util.concurrent.PriorityBlockingQueue

class EventMessaging {

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Listener<Event>>>()

    // Events might arrive from main and web-socket threads, so gotta use a thread safe queue.
    private val events = PriorityBlockingQueue<Event>()

    /**
     * Register a concrete event listener.
     */
    fun <T : Event> listen(type: Class<T>, listener: Listener<T>) {
        var existing = listeners[type]
        if (existing == null) {
            existing = mutableListOf()
            listeners[type] = existing
        }

        @Suppress("UNCHECKED_CAST")
        existing.add(listener as Listener<Event>)
    }

    /**
     * Register a listener by providing a listener function.
     */
    inline fun <reified T : Event> listen(crossinline listener: (T) -> Unit) {
        listen(object : Listener<T> {
            override fun listen(event: T) {
                listener(event)
            }
        })
    }

    /**
     * Register a listener.
     */
    inline fun <reified T : Event> listen(listener: Listener<T>) {
        listen(T::class.java, listener)
    }

    /**
     * Process all pending events one by one.
     */
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

    /**
     * Send an event to event queue. Note that sending an event doesn't mean it will be processed
     * during current tick.
     */
    fun <T : Event> send(event: T) {
        events.offer(event)
    }
}