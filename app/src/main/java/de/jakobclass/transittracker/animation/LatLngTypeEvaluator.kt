package de.jakobclass.transittracker.animation

import android.animation.TypeEvaluator
import com.google.android.gms.maps.model.LatLng
import de.jakobclass.transittracker.utilities.let

class LatLngTypeEvaluator: TypeEvaluator<LatLng> {
    override fun evaluate(fraction: Float, startPosition: LatLng?, endPosition: LatLng?): LatLng? {
        return let(startPosition, endPosition) { startPosition, endPosition ->
            val latitude = (endPosition.latitude - startPosition.latitude) * fraction + startPosition.latitude;
            val longitude = (endPosition.longitude - startPosition.longitude) * fraction + startPosition.longitude;
            return@let LatLng(latitude, longitude);
        }
    }
}