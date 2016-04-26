package de.jakobclass.transittracker.models

data class Route(val vehicleType: VehicleType, val stops: Collection<Stop>) {
}