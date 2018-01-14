package com.edd.knug.util.messaging

interface Listener<in T : Event> {

    fun listen(event: T)
}