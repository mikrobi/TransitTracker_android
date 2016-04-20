package de.jakobclass.transittracker.models

data class Vehicle(val vehicleId: String, val type: VehicleType, val name: String, val destination: String, var position: Position) {
    var predictedPositions = listOf<Position>()
}