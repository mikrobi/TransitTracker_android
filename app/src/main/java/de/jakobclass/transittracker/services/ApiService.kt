package de.jakobclass.transittracker.services

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.services.stops.StopService
import de.jakobclass.transittracker.services.stops.StopServiceDelegate
import java.lang.ref.WeakReference

interface ApiServiceDelegate {
    fun apiServiceDidAddStops(stops: List<Stop>)
}

class ApiService(val stopService: StopService = StopService()): StopServiceDelegate {
    var boundingBox: LatLngBounds? = null
        set(value) {
            field = value
            fetchStops()
        }
    var delegate: ApiServiceDelegate?
        get() = delegateReference?.get()
        set(value) { delegateReference = WeakReference<ApiServiceDelegate>(value)
        }
    val stops: Collection<Stop>
        get() = stopService.stops.values
    var vehicleTypes = listOf<VehicleType>(VehicleType.Bus,
            VehicleType.StreetCar,
            VehicleType.SuburbanTrain,
            VehicleType.Subway)

    private var delegateReference = WeakReference<ApiServiceDelegate>(null)

    init {
        stopService.delegate = this
    }

    private fun fetchStops() {
        boundingBox?.let { stopService.fetchStops(it, vehicleTypes) }
    }

    override fun stopServiceDidAddStops(stops: List<Stop>) {
        delegate?.apiServiceDidAddStops(stops)
    }
}