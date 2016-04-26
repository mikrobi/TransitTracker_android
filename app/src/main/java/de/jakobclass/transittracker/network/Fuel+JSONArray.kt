package de.jakobclass.transittracker.network

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import org.json.JSONArray

fun Request.responseJsonArray(handler: (Request, Response, Result<JSONArray, FuelError>) -> Unit) =
        response(jsonArrayDeserializer(), handler)

fun Request.responseJsonArray(handler: Handler<JSONArray>) = response(jsonArrayDeserializer(), handler)

fun Request.responseJsonArray() = response(jsonArrayDeserializer())

fun jsonArrayDeserializer(): Deserializable<JSONArray> {
    return object : Deserializable<JSONArray> {
        override fun deserialize(response: Response): JSONArray {
            return JSONArray(String(response.data))
        }
    }
}
