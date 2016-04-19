package de.jakobclass.transittracker.services

import android.os.AsyncTask
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.models.VehicleType
import de.jakobclass.transittracker.utilities.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

interface StopParsingTaskDelegate {
    val stops: Map<String, Stop>

    fun addStops(stops: List<Stop>)
}

class StopParsingTask(delegate: StopParsingTaskDelegate) : AsyncTask<JSONObject, Void, List<Stop>>() {
    var delegate: StopParsingTaskDelegate?
        get() = delegateReference.get()
        set(value) { delegateReference = WeakReference<StopParsingTaskDelegate>(value) }

    private var delegateReference = WeakReference<StopParsingTaskDelegate>(null)

    init {
        this.delegate = delegate
    }

    override fun doInBackground(vararg data: JSONObject?): List<Stop>? {
        var stops = mutableListOf<Stop>()
        val data = data.first()
        data?.let {
            val stopsData = data.getJSONArray("stops")
            if (stopsData.length() > 1) {
                if (isCancelled) {
                    return null
                }
                // we skip the first element because it's something like a column descriptor:
                // ["x","y","viewmode","name","extId","puic","prodclass","lines"]
                for (i in 1..(stopsData.length() - 1)) {
                    val stopData = stopsData.getJSONArray(i)
                    val name = stopData.getString(3)
                    if (delegate?.stops?.containsKey(name) ?: false) {
                        continue
                    }
                    val stop = Stop(stopData)
                    stop?.let {
                        stops.add(stop)
                    }
                }
            }
        }
        return stops
    }

    override fun onPostExecute(stops: List<Stop>?) {
        delegate?.addStops(stops ?: listOf<Stop>())
    }
}

fun Stop(data: JSONArray): Stop? {
    try {
        val x = data.getInt(0)
        val y = data.getInt(1)
        val name = data.getString(3)
        val stop = Stop(LatLng(x = x, y = y), name)

        val vehicleTypesData = data.getJSONArray(6)
        val vehicleTypes = mutableListOf<VehicleType>()
        for (i in 0..(vehicleTypesData.length() - 1)) {
            val vehicleType = VehicleType.fromCode(vehicleTypesData.getInt(i))
            vehicleType?.let { vehicleTypes.add(it) }
        }
        stop.vehicleTypes = vehicleTypes

        val linesData = data.getJSONArray(7)
        val lines = mutableListOf<String>()
        for (i in 0..(linesData.length() - 1)) {
            val lineData = linesData.getJSONArray(i)
            val vehicleTypeName = lineData.getString(0)
            var name = lineData.getString(1)
            val vehicleType = VehicleType.fromCode(lineData.getInt(2))
            if (vehicleType == VehicleType.Bus) {
                name = "$vehicleTypeName $name"
            }
            lines.add(name)
        }
        stop.lines = lines

        return stop
    } catch (e: JSONException) {
        return null
    }
}