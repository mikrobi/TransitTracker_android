package de.jakobclass.transittracker.services

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.network.Api
import java.util.*

interface StopServiceDelegate {
    fun stopServiceDidAddStops(stops: List<Stop>)
}

class StopService: StopParsingTaskDelegate {
    var delegate: StopServiceDelegate? = null
    override val stops: Map<String, Stop> get() = _stops
    var vehilceTypes = arrayOf(VehicleType.Bus, VehicleType.StreetCar, VehicleType.SuburbanTrain, VehicleType.Subway)

    private val _stops = mutableMapOf<String, Stop>()

    fun fetchStops(boundingBox: LatLngBounds) {
        val vehicleTypesCode: Int = vehilceTypes.fold(0) { sum, vehicleType -> sum + vehicleType.code }
        var parameters = boundingBox.asRequestParameters()
        parameters["look_stopclass"] = vehicleTypesCode
        parameters["tpl"] = "stop2shortjson"
        parameters["performLocating"] = 2
        parameters["look_nv"] = "get_shortjson|yes|get_lines|yes|combinemode|1|density|26|"

        Api.request(parameters) { data ->
            val stopParsingTask = StopParsingTask(this)
            stopParsingTask.execute(data)
        }
    }

    override fun addStops(stops: List<Stop>) {
        var addedStops = mutableListOf<Stop>()
        for (stop in stops) {
            if (!_stops.containsKey(stop.name)) {
                _stops.set(stop.name, stop)
                addedStops.add(stop)
            }
        }
        delegate?.stopServiceDidAddStops(addedStops)
    }
}

fun LatLngBounds.asRequestParameters(): HashMap<String, Any> {
    return hashMapOf("look_minx" to southwest.x,
            "look_miny" to southwest.y,
            "look_maxx" to  northeast.x,
            "look_maxy" to northeast.y)
}

