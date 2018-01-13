package com.edd.knug.messaging

interface Listener<in T : Event> {

    fun listen(event: T)
}