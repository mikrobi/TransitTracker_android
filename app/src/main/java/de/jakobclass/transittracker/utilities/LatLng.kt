package de.jakobclass.transittracker.utilities

import com.google.android.gms.maps.model.LatLng

val COORDINATE_FACTOR = 1000000

val LatLng.x: Int get() = (longitude * COORDINATE_FACTOR).toInt()
val LatLng.y: Int get() = (latitude * COORDINATE_FACTOR).toInt()

fun LatLng(x: Int, y: Int) = LatLng(y.toDouble() / COORDINATE_FACTOR, x.toDouble() / COORDINATE_FACTOR)