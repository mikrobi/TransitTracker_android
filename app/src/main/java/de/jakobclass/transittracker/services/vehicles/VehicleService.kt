package de.jakobclass.transittracker.services.vehicles

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Position
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.network.Api
import de.jakobclass.transittracker.services.asRequestParameters
import java.lang.ref.WeakReference
import java.util.*

interface VehicleServiceDelegate {
    fun vehicleServiceDidAddVehicles(vehicles: Collection<Vehicle>)
    fun vehicleServiceDidRemoveVehicles(vehicles: Collection<Vehicle>)
}

class VehicleService : VehicleParsingTaskDelegate {
    var delegate: VehicleServiceDelegate?
        get() = delegateReference.get()
        set(value) {
            delegateReference = WeakReference<VehicleServiceDelegate>(value)
        }
    override val vehicles: Map<String, Vehicle>
        get() = _vehicles

    private var activeVehicleParsingTask: VehicleParsingTask? = null
    private var delegateReference = WeakReference<VehicleServiceDelegate>(null)
    private var _vehicles = mutableMapOf<String, Vehicle>()

    fun fetchVehicles(boundingBox: LatLngBounds, vehicleTypesCode: Int, fetchInterval: Int, updateInterval: Int) {
        var parameters = boundingBox.asRequestParameters()
        parameters["look_productclass"] = vehicleTypesCode
        parameters["look_json"] = "yes"
        parameters["tpl"] = "trains2json2"
        parameters["performLocating"] = 1
        parameters["look_nv"] = "zugposmode|2|interval|$fetchInterval|intervalstep|$updateInterval|"

        Api.request(parameters) { data ->
            activeVehicleParsingTask?.cancel(false)
            activeVehicleParsingTask = VehicleParsingTask(this)
            activeVehicleParsingTask!!.execute(data)
        }
    }

    override fun addOrUpdateVehiclesAndPositions(vehiclesAndPositions: Map<Vehicle, LinkedList<Position>>) {
        var addedVehicles = mutableSetOf<Vehicle>()
        var updatedVehicles = mutableSetOf<Vehicle>()
        for ((vehicle, positions) in vehiclesAndPositions) {
            vehicle.predictedPositions = positions
            if (_vehicles.containsKey(vehicle.vehicleId)) {
                updatedVehicles.add(vehicle)
            } else {
                _vehicles[vehicle.vehicleId] = vehicle
                addedVehicles.add(vehicle)
            }
        }
        val removedVehicles = _vehicles.values.toSet().subtract(addedVehicles).subtract(updatedVehicles)
        for (vehicle in removedVehicles) {
            _vehicles.remove(vehicle.vehicleId)
        }
        delegate?.vehicleServiceDidAddVehicles(addedVehicles)
        delegate?.vehicleServiceDidRemoveVehicles(removedVehicles)
        updateVehiclePositions()
    }

    fun updateVehiclePositions() {
        for (vehicle in vehicles.values) {
            vehicle.updatePosition()
        }
    }
}