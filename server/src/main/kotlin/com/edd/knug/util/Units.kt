package com.edd.knug.util

/**
 * @return number value as world units, assuming that current value is in visual units.
 */
val Number.worldUnits: Double
    get() = toDouble() * MPP

/**
 * @return number value as visual units, assuming that current value is in world units.
 */
val Number.visualUnits: Double
    get() = toDouble() * PPM