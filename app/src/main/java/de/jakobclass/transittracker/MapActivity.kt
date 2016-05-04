package de.jakobclass.transittracker

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Property
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import de.jakobclass.transittracker.animation.LatLngTypeEvaluator
import de.jakobclass.transittracker.models.Stop
import de.jakobclass.transittracker.factories.BitmapFactory
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.models.VehicleDelegate
import de.jakobclass.transittracker.services.ApiService
import de.jakobclass.transittracker.services.ApiServiceDelegate
import de.jakobclass.transittracker.utilities.BiMap

class MapActivity : FragmentActivity(), OnMapReadyCallback, ConnectionCallbacks,
        OnCameraChangeListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        ApiServiceDelegate, VehicleDelegate {
    private val apiService: ApiService
        get() = (application as Application).apiService
    private var googleApiClient: GoogleApiClient? = null
    private var highlightedRoute: Polyline? = null
    private var map: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment
    private val vehicleMarkers = BiMap<Vehicle, Marker>()

    private val REQUEST_CODE_ACCESS_FINE_LOCATION = 1
    private val REQUIRED_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    private val screenDensity: Float
        get() = resources.displayMetrics.density

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        apiService.delegate = this
    }

    override fun onStop() {
        googleApiClient?.disconnect()

        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.setOnCameraChangeListener(this)
        map!!.setOnMarkerClickListener(this)
        map!!.setOnMapClickListener(this)
        addMarkersForStops(apiService.stops)
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
        updateBoundingBox()
    }

    private fun updateBoundingBox() {
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
            apiService.boundingBox = LatLngBounds(southWest, northEast)
        }
    }

    override fun apiServiceDidAddStops(stops: List<Stop>) {
        addMarkersForStops(stops)
    }

    private fun addMarkersForStops(stops: Collection<Stop>) {
        for (stop in stops) {
            val bitmap = BitmapFactory.bitmapForStop(stop, screenDensity)
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            map?.addMarker(MarkerOptions()
                    .position(stop.coordinate)
                    .title(stop.name)
                    .snippet(stop.lines.joinToString(", "))
                    .icon(icon))
        }
    }

    override fun apiServiceDidAddVehicles(vehicles: Collection<Vehicle>) {
        for (vehicle in vehicles) {
            vehicle.delegate = this
            val bitmap = BitmapFactory.bitmapForVehicle(vehicle, screenDensity)
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            map?.addMarker(MarkerOptions()
                    .position(vehicle.position.coordinate)
                    .title(vehicle.name)
                    .snippet(vehicle.destination)
                    .icon(icon)
                    .anchor(0.5f, 0.5f))
                    ?.let { vehicleMarkers[vehicle] = it }
        }
    }

    override fun apiServiceDidRemoveVehicles(vehicles: Collection<Vehicle>) {
        for (vehicle in vehicles) {
            vehicleMarkers.remove(vehicle)?.remove()
        }
    }

    override fun onVehiclePositionUpdate(vehicle: Vehicle) {
        vehicleMarkers[vehicle]?.let {
            val bitmap = BitmapFactory.bitmapForVehicle(vehicle, screenDensity)
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            it.setIcon(icon)
            val property = Property.of(Marker::class.java, LatLng::class.java, "position");
            val animator = ObjectAnimator.ofObject(it, property, LatLngTypeEvaluator(), vehicle.position.coordinate)
            animator.duration = apiService.positionUpdateIntervalInMS.toLong()
            animator.interpolator = null
            animator.start()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            vehicleMarkers.getKey(it)?.let {
                apiService.fetchRouteAndStops(it) { route ->
                    highlightedRoute?.remove()
                    val options = PolylineOptions()
                            .addAll(route.coordinates)
                            .color(route.vehicleType.color)
                            .width(6.0f * screenDensity)
                    highlightedRoute = map?.addPolyline(options)
                }
            }
        }

        return false
    }

    override fun onMapClick(coordinate: LatLng?) {
        highlightedRoute?.remove()
        highlightedRoute = null
    }
}

val Location.isInBerlinArea: Boolean get() = latitude < 52.833702 && latitude > 52.250741 && longitude < 13.948642 && longitude > 12.873355