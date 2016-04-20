package de.jakobclass.transittracker.models

import com.google.android.gms.maps.model.LatLng

data class Position(val coordinate: LatLng, val direction: Int?) {
}