package com.edd.knug.util.loop

import java.util.concurrent.TimeUnit

/**
 * Game loop with a fixed time step.
 *
 * @see <a href="http://www.java-gaming.org/topics/game-loops/24220/view">Game loops!</a>
 */
internal class FixedTimeStepLoop(
        tickRate: Number = 30,
        private val update: (frameTime: Double) -> Unit = {}
) : GameLoop {

    /**
     * Constant update interval.
     */
    private val interval : Double

    /**
     * Is the loop is running or not.
     */
    var running = true

    init {
        val doubleTickRate = tickRate.toDouble()
        if (doubleTickRate <= 0.0) {
            throw IllegalArgumentException("tickRage must be greater than 0")
        }
        interval = TimeUnit.SECONDS.toNanos(1) / doubleTickRate
    }

    override fun run() {
        var lastUpdate = System.nanoTime().toDouble()

        while (running) {
            var now = System.nanoTime()

            while (now - lastUpdate > interval) {
                update(interval)
                lastUpdate += interval
            }

            if (now - lastUpdate > interval) {
                lastUpdate = now - interval
            }

            while (now - lastUpdate < interval) {

                // Sleeping helps free up the CPU a bit.
                Thread.sleep(1)
                now = System.nanoTime()
            }
        }
    }
}