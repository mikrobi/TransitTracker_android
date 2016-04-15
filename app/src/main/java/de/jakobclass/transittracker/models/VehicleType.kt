package de.jakobclass.transittracker.models

enum class VehicleType(val code: Int) {
    SuburbanTrain(1),
    Subway(2),
    StreetCar(4),
    Bus(8),
    Ferry(16),
    LongDistanceTrain(32),
    RegionalTrain(64)
}