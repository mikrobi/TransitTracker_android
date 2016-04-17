package de.jakobclass.transittracker.models

import android.graphics.Color

enum class VehicleType(val code: Int) {
    SuburbanTrain(1),
    Subway(2),
    StreetCar(4),
    Bus(8),
    Ferry(16),
    LongDistanceTrain(32),
    RegionalTrain(64);

    companion object {
        fun fromCode(code: Int): VehicleType? = VehicleType.values().first { it.code == code }
    }

    val color: Int
        get() = when (this) {
            SuburbanTrain -> Color.argb(255, 16, 196, 0)
            Subway -> Color.argb(255, 0, 135, 232)
            StreetCar -> Color.argb(255, 224, 0, 0)
            Bus -> Color.argb(255, 152, 0, 175)
            Ferry -> Color.argb(255, 249, 237, 0)
            LongDistanceTrain -> Color.argb(255, 0, 0, 0)
            RegionalTrain -> Color.argb(255, 0, 0, 0)
        }

    val abbreviation: String
        get() = when (this) {
            SuburbanTrain -> "S"
            Subway -> "U"
            StreetCar -> "T"
            Bus -> "B"
            Ferry -> "F"
            LongDistanceTrain -> "Z"
            RegionalTrain -> "R"
        }
}