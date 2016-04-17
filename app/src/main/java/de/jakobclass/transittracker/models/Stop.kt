package de.jakobclass.transittracker.models

import com.google.android.gms.maps.model.LatLng

data class Stop(val coordinate: LatLng,
                val name: String)