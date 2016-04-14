package de.jakobclass.transittracker

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapActivity : FragmentActivity(), OnMapReadyCallback, ConnectionCallbacks {

    private var map: GoogleMap? = null
    private var googleApiClient: GoogleApiClient? = null

    private val REQUEST_CODE_ACCESS_FINE_LOCATION = 1
    private val REQUIRED_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStop() {
        googleApiClient?.disconnect()
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val berlinCityCenter = LatLng(52.520048, 13.404773)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(berlinCityCenter, 16.0f))
        initLocationPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION && permissions.isNotEmpty()
                && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }

    private fun initLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, REQUIRED_LOCATION_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_LOCATION_PERMISSION)) {
            AlertDialog.Builder(this).setMessage(R.string.location_usage_description)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok) { dialog, index -> requestLocationPermission() }
                    .show()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(REQUIRED_LOCATION_PERMISSION), REQUEST_CODE_ACCESS_FINE_LOCATION)
    }

    private fun enableMyLocation() {
        map?.isMyLocationEnabled = true
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build()
        }
        googleApiClient?.connect()
    }

    override fun onConnected(connectionHint: Bundle?) {
        val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        lastLocation?.let {
            if (lastLocation.isInBerlinArea) {
                val lastLocationLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLng(lastLocationLatLng))
            }
        }
    }

    override fun onConnectionSuspended(cause: Int) {
        // Nothing to do
    }

}

val Location.isInBerlinArea: Boolean get() = latitude < 52.833702 && latitude > 52.250741 && longitude < 13.948642 && longitude > 12.873355