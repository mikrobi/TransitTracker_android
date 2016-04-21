package de.jakobclass.transittracker.services.vehicles

import android.os.AsyncTask
import de.jakobclass.transittracker.models.Position
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.utilities.LatLng
import de.jakobclass.transittracker.utilities.let
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*

interface VehicleParsingTaskDelegate {
    val vehicles: Map<String, Vehicle>

    fun addOrUpdateVehiclesAndPositions(vehiclesAndPositions: Map<Vehicle, LinkedList<Position>>)
}

class VehicleParsingTask(delegate: VehicleParsingTaskDelegate): AsyncTask<JSONObject, Void, Map<Vehicle, LinkedList<Position>>>() {
    var delegate: VehicleParsingTaskDelegate?
        get() = delegateReference.get()
        set(value) { delegateReference = WeakReference<VehicleParsingTaskDelegate>(value)
        }

    private var delegateReference = WeakReference<VehicleParsingTaskDelegate>(null)

    init {
        this.delegate = delegate
    }

    override fun doInBackground(vararg data: JSONObject?): Map<Vehicle, LinkedList<Position>>? {
        return data.first()?.let { parseVehiclePositionsFromJSON(it) }
    }

    override fun onPostExecute(vehiclesAndPositions: Map<Vehicle, LinkedList<Position>>?) {
        vehiclesAndPositions?.let { delegate?.addOrUpdateVehiclesAndPositions(it) }
    }

    private fun parseVehiclePositionsFromJSON(data: JSONObject): Map<Vehicle, LinkedList<Position>>? {
        var vehiclePositions = mutableMapOf<Vehicle, LinkedList<Position>>()
        val vehiclesData = data.getJSONArray("t")
        for (i in 0..(vehiclesData.length() - 1)) {
            if (isCancelled) {
                return null
            }
            val vehicleData = vehiclesData.getJSONObject(i)
            findOrCreateVehicle(vehicleData)?.let {
                vehiclePositions[it] = getPositionsFromJSON(vehicleData)
            }
        }
        return vehiclePositions
    }

    private fun findOrCreateVehicle(data: JSONObject): Vehicle? {
        val vehicleId = data.getString("i")
        return delegate?.vehicles?.get(vehicleId) ?: Vehicle(vehicleId, data)
    }

    private fun getPositionsFromJSON(data: JSONObject): LinkedList<Position> {
        val positionsData = data.getJSONArray("p")
        val positions = LinkedList<Position>()
        for (i in 0..(positionsData.length() - 1)) {
            val positionData = positionsData.getJSONObject(i)
            Position(positionData)?.let { positions.add(it) }
        }
        return positions
    }
}

fun Vehicle(vehicleId: String, data: JSONObject): Vehicle? {
    try {
        val vehicleType = VehicleType.fromCode(data.getInt("c"))
        val position = Position(data)
        return let(vehicleType, position) { vehicleType, position ->
            val name = data.getString("n").trim()
            val destination = data.getString("l")
            return@let Vehicle(vehicleId, vehicleType, name, destination, position)
        }
    } catch (e: JSONException) {
        return null
    }
}

fun Position(data: JSONObject): Position? {
    try {
        val x = data.getInt("x")
        val y = data.getInt("y")
        var direction: Int? = null
        if (data.has("d")) {
            direction = data.getInt("d")
        }
        return Position(LatLng(x, y), direction)
    } catch (e: JSONException) {
        return null
    }
}