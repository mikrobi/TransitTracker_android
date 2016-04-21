package de.jakobclass.transittracker.utilities

import android.graphics.Point

fun Point.neighbourWithDistance(distance: Float, angle: Double): Point {
    val _x = x + distance * Math.sin(angle)
    val _y = y + distance * Math.cos(angle)
    return Point(_x.toInt(), _y.toInt())
}