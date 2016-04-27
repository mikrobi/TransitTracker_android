package de.jakobclass.transittracker.services.routes

import de.jakobclass.transittracker.models.Route
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.network.Api

class RouteService {
    fun fetchRouteAndStops(vehicle: Vehicle, completion: (route: Route) -> Unit) {
        val parameters = mapOf<String, Any>(
                "look_trainid" to vehicle.vehicleId,
                "tpl" to  "chain2json3",
                "performLocating" to 16,
                "format_xy_n" to "true")
        val route = vehicle.route
        if (route is Route) {
            completion(route)
        } else {
            Api.requestJSONArray(parameters) { data ->
                val parsingTask = RouteParsingTask(vehicle , completion)
                parsingTask.execute(data)
            }
        }
    }
}