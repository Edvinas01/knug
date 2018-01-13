package com.edd.knug.util.loop

interface GameLoop : Runnable {

    /**
     * Run the game loop.
     */
    override fun run()
}