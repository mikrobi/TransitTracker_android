package de.jakobclass.transittracker.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.success
import org.json.JSONObject

class Api {
    companion object {
        fun request(parameters: Map<String, Any>, callback: (data: JSONObject) -> Unit): Request {
            return Fuel.get("http://fahrinfo.vbb.de/bin/query.exe/dny", parameters.toList()).responseJson { request, response, result ->
                result.success { data -> callback(data) }
            }
        }
    }
}
