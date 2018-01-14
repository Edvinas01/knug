package com.edd.knug.util

// TODO: move these to some config file.

/**
 * Game web-socket endpoint.
 */
const val WEB_SOCKET_ENDPOINT = "/game"

/**
 * Timeout for idle web-socket connections in millis.
 */
const val SESSION_IDLE_TIMEOUT = 10000L

/**
 * Main game loop tick rate.
 */
const val TICK_RATE = 1

/**
 * Pixels per meter.
 */
const val PPM = 100.0

/**
 * Meters per pixel.
 */
const val MPP = 1 / PPM

/**
 * Player width in visual units.
 */
const val PLAYER_WIDTH = 64

/**
 * Player height in visual units.
 */
const val PLAYER_HEIGHT = 64

/**
 * World width in visual units.
 */
const val WORLD_WIDTH = 2000

/**
 * World height in visual units.
 */
const val WORLD_HEIGHT = 2000