package de.jakobclass.transittracker.services

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.network.Api
import java.util.*

class StopService {

    var vehilceTypes = arrayOf(VehicleType.Bus, VehicleType.StreetCar, VehicleType.SuburbanTrain, VehicleType.Subway)

    fun fetchStops(boundingBox: LatLngBounds) {
        val vehicleTypesCode: Int = vehilceTypes.fold(0) { sum, vehicleType -> sum + vehicleType.code }
        var parameters = boundingBox.asRequestParameters()
        parameters["look_stopclass"] = vehicleTypesCode
        parameters["tpl"] = "stop2shortjson"
        parameters["performLocating"] = 2
        parameters["look_nv"] = "get_shortjson|yes|get_lines|yes|combinemode|1|density|26|"

        Api.request(parameters) {}
    }
}

fun LatLngBounds.asRequestParameters(): HashMap<String, Any> {
    return hashMapOf("look_minx" to southwest.x,
            "look_miny" to southwest.y,
            "look_maxx" to  northeast.x,
            "look_maxy" to northeast.y)
}

val LatLng.x: Int get() = (longitude * 1000000).toInt()
val LatLng.y: Int get() = (latitude * 1000000).toInt()