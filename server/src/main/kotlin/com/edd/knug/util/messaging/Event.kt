package com.edd.knug.util.messaging

interface Event : Comparable<Event> {

    override fun compareTo(other: Event) = 0
}