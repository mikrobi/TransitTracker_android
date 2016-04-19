package de.jakobclass.transittracker.services.vehicles

import com.google.android.gms.maps.model.LatLngBounds
import de.jakobclass.transittracker.network.Api
import de.jakobclass.transittracker.services.asRequestParameters

class VehicleService {
    fun fetchVehicles(boundingBox: LatLngBounds, vehicleTypesCode: Int, fetchInterval: Int, updateInterval: Int) {
        var parameters = boundingBox.asRequestParameters()
        parameters["look_productclass"] = vehicleTypesCode
        parameters["look_json"] = "yes"
        parameters["tpl"] = "trains2json2"
        parameters["performLocating"] = 1
        parameters["look_nv"] = "zugposmode|2|interval|$fetchInterval|intervalstep|$updateInterval|"

        Api.request(parameters) { data ->
            // ToDo: Parse the vehicle JSON
        }
    }

}