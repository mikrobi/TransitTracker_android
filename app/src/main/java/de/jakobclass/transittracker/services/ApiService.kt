package de.jakobclass.transittracker.services

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.services.stops.StopService
import de.jakobclass.transittracker.services.stops.StopServiceDelegate
import de.jakobclass.transittracker.services.vehicles.VehicleService
import de.jakobclass.transittracker.utilities.x
import de.jakobclass.transittracker.utilities.y
import java.lang.ref.WeakReference
import java.util.*

interface ApiServiceDelegate {
    fun apiServiceDidAddStops(stops: List<Stop>)
}

class ApiService(val stopService: StopService = StopService(),
                 val vehicleService: VehicleService = VehicleService()): StopServiceDelegate {
    var boundingBox: LatLngBounds? = null
        set(value) {
            field = value
            fetchStops()
            fetchVehicles()
        }
    var delegate: ApiServiceDelegate?
        get() = delegateReference.get()
        set(value) { delegateReference = WeakReference<ApiServiceDelegate>(value)
        }
    val stops: Collection<Stop>
        get() = stopService.stops.values
    var vehicleTypes = listOf<VehicleType>(VehicleType.Bus,
            VehicleType.StreetCar,
            VehicleType.SuburbanTrain,
            VehicleType.Subway)

    private var delegateReference = WeakReference<ApiServiceDelegate>(null)
    private val vehicleFetchIntervalInMS: Int = 20000
    private val vehiclePositionUpdateIntervalInMS: Int = 2000
    private val vehicleTypesCode: Int
        get() = vehicleTypes.fold(0) { sum, vehicleType -> sum + vehicleType.code }

    init {
        stopService.delegate = this
    }

    private fun fetchStops() {
        boundingBox?.let { stopService.fetchStops(it, vehicleTypesCode) }
    }

    private fun fetchVehicles() {
        boundingBox?.let {
            vehicleService.fetchVehicles(it, vehicleTypesCode,
                    vehicleFetchIntervalInMS, vehiclePositionUpdateIntervalInMS)
        }
    }

    override fun stopServiceDidAddStops(stops: List<Stop>) {
        delegate?.apiServiceDidAddStops(stops)
    }
}

fun LatLngBounds.asRequestParameters(): HashMap<String, Any> {
    return hashMapOf("look_minx" to southwest.x,
            "look_miny" to southwest.y,
            "look_maxx" to  northeast.x,
            "look_maxy" to northeast.y)
}