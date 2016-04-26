package de.jakobclass.transittracker.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.success
import org.json.JSONArray
import org.json.JSONObject

class Api {
    companion object {
        fun requestJSONObject(parameters: Map<String, Any>, callback: (data: JSONObject) -> Unit): Request {
            return request(parameters).responseJson { request, response, result ->
                result.success { data -> callback(data) }
            }
        }

        fun requestJSONArray(parameters: Map<String, Any>, callback: (data: JSONArray) -> Unit): Request {
            return request(parameters).responseJsonArray { request, response, result ->
                result.success { data -> callback(data) }
            }
        }

        private fun request(parameters: Map<String, Any>): Request {
            return Fuel.get("http://fahrinfo.vbb.de/bin/query.exe/dny", parameters.toList())
        }
    }
}
