package de.jakobclass.transittracker.services.vehicles

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.models.Position
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.network.Api
import de.jakobclass.transittracker.services.asRequestParameters

class VehicleService : VehicleParsingTaskDelegate {
    override val vehicles: Map<String, Vehicle>
        get() = _vehicles

    private var activeVehicleParsingTask: VehicleParsingTask? = null
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

    override fun addOrUpdateVehiclesAndPositions(vehiclesAndPositions: Map<Vehicle, List<Position>>) {

    }
}