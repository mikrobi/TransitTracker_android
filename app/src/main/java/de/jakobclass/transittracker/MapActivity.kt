package de.jakobclass.transittracker

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Point
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
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.services.StopService
import de.jakobclass.transittracker.services.StopServiceDelegate

class MapActivity : FragmentActivity(), OnMapReadyCallback, ConnectionCallbacks, OnCameraChangeListener, StopServiceDelegate {
    private var map: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment
    private var googleApiClient: GoogleApiClient? = null
    private var stopService = StopService()

    private val REQUEST_CODE_ACCESS_FINE_LOCATION = 1
    private val REQUIRED_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        stopService.delegate = this
    }

    override fun onStop() {
        googleApiClient?.disconnect()

        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopService.delegate = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.setOnCameraChangeListener(this)
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

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        fetchStops()
    }

    private fun fetchStops() {
        var mapView = mapFragment.view
        mapView?.let {
            val mapWidth = mapView.width.toInt()
            val mapHeight = mapView.height.toInt()
            val mapX = mapView.x.toInt()
            val mapY = mapView.y.toInt()
            val northEastPoint = Point(mapX + 2 * mapWidth, mapY - mapHeight)
            val southWestPoint = Point(mapX - mapWidth, mapY + 2 * mapHeight)
            val northEast = map!!.projection.fromScreenLocation(northEastPoint)
            val southWest = map!!.projection.fromScreenLocation(southWestPoint)
            val boundingBox = LatLngBounds(southWest, northEast)
            stopService.fetchStops(boundingBox)
        }
    }

    override fun stopServiceDidAddStops(stops: List<Stop>) {
        for (stop in stops) {
            map?.addMarker(MarkerOptions().position(stop.coordinate).title(stop.name))
        }
    }
}

val Location.isInBerlinArea: Boolean get() = latitude < 52.833702 && latitude > 52.250741 && longitude < 13.948642 && longitude > 12.873355