package de.jakobclass.transittracker.services.routes

import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import de.jakobclass.transittracker.models.Route
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.utilities.LatLng
import org.json.JSONArray

class RouteParsingTask(val vehicle: Vehicle, val completion: (route: Route) -> Unit): AsyncTask<JSONArray, Void, Pair<Collection<LatLng>, Collection<Stop>>>() {
    override fun doInBackground(vararg data: JSONArray?): Pair<Collection<LatLng>, Collection<Stop>>? {
        val coordinates = mutableListOf<LatLng>()
        val stops = mutableListOf<Stop>()
        data.first()?.let {
            val coordinatesData = it.getJSONArray(0)
            for (i in 0..(coordinatesData.length() - 1)) {
                val coordinateData = coordinatesData.getJSONArray(i)
                val coordinate = LatLng(coordinateData.getInt(0), coordinateData.getInt(1))
                coordinates.add(coordinate)
            }
            val stopsData = it.getJSONArray(1)
            for (i in 0..(stopsData.length() - 1)) {
                val stopData = stopsData.getJSONArray(i)
                val coordinate = coordinates[stopData.getInt(0)]
                val name = stopData.getString(1)
                stops.add(Stop(coordinate, name))
            }
        }

        return Pair(coordinates, stops)
    }

    override fun onPostExecute(result: Pair<Collection<LatLng>, Collection<Stop>>?) {
        result?.let {
            val route = Route(result.first, result.second, vehicle.type)
            vehicle.route = route
            completion(route)
        }
    }
}