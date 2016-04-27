package de.jakobclass.transittracker.models

import com.google.android.gms.maps.model.LatLng

data class Route(val coordinates: Collection<LatLng>, val stops: Collection<Stop>, val vehicleType: VehicleType) {
}