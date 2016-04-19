package de.jakobclass.transittracker.services.stops

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.network.Api
import de.jakobclass.transittracker.utilities.x
import de.jakobclass.transittracker.utilities.y
import java.lang.ref.WeakReference
import java.util.*

interface StopServiceDelegate {
    fun stopServiceDidAddStops(stops: List<Stop>)
}

class StopService: StopParsingTaskDelegate {
    var delegate: StopServiceDelegate?
        get() = delegateReference?.get()
        set(value) { delegateReference = WeakReference<StopServiceDelegate>(value)
        }
    override val stops: Map<String, Stop>
        get() = _stops

    private val _stops = mutableMapOf<String, Stop>()
    private var aktiveStopParsingTask: StopParsingTask? = null
    private var delegateReference = WeakReference<StopServiceDelegate>(null)

    fun fetchStops(boundingBox: LatLngBounds, vehilceTypes: List<VehicleType>) {
        val vehicleTypesCode: Int = vehilceTypes.fold(0) { sum, vehicleType -> sum + vehicleType.code }
        var parameters = boundingBox.asRequestParameters()
        parameters["look_stopclass"] = vehicleTypesCode
        parameters["tpl"] = "stop2shortjson"
        parameters["performLocating"] = 2
        parameters["look_nv"] = "get_shortjson|yes|get_lines|yes|combinemode|1|density|26|"

        Api.request(parameters) { data ->
            aktiveStopParsingTask?.cancel(false)
            aktiveStopParsingTask = StopParsingTask(this)
            aktiveStopParsingTask!!.execute(data)
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

