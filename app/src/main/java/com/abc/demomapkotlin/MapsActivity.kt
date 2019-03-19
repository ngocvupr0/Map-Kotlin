package com.abc.demomapkotlin

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.abc.demomapkotlin.drawDirection.DirectionFinder
import com.abc.demomapkotlin.drawDirection.DirectionFinderListener
import com.abc.demomapkotlin.drawDirection.Route
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.dialog_search_direction.*
import java.io.UnsupportedEncodingException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
    DirectionFinderListener {

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location

    private lateinit var latLngOrigin: LatLng
    private lateinit var latLngDestination: LatLng
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var mMaker: Marker? = null

    //location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    //camera
    private var cameraZoomMyLocation: Float = 18f
    private var cameraZoomDirection: Float = 15f

    //Direction
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var mGeoDataClient: GeoDataClient
    private var LAT_LNG_BOUNDS: LatLngBounds? = null
    private var isAutoCompleteLocation: Boolean = false

    //Draw Direction
    private var originMarkers: MutableList<Marker>? = ArrayList()
    private var destinationMarkers: MutableList<Marker>? = ArrayList()
    private var polylinePaths: MutableList<Polyline>? = ArrayList()
    private var progressDialog: ProgressDialog? = null

    companion object {
        private val MY_PERMISSION_CODE: Int = 1000
    }

    private var img_direction: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mGeoDataClient = Places.getGeoDataClient(this, null)

        img_direction = findViewById(R.id.img_direction)

        //Request runtime Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        } else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations[p0!!.locations.size - 1] // get last location

                if (mMaker != null) {
                    mMaker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude, longitude)
//                val markerOptions = MarkerOptions()
//                    .position(latLng)
//                    .title("Your Location")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                mMaker = mMap!!.addMarker(markerOptions)

                //move camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(cameraZoomMyLocation))

                LAT_LNG_BOUNDS = LatLngBounds(latLng, latLng)
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = cameraZoomDirection
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE
                )
            }

            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )

                            mMap!!.isMyLocationEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Enable Zoom Control
        mMap.uiSettings.isZoomControlsEnabled = true
//        mMap.uiSettings.isMyLocationButtonEnabled = false

        //Init google play services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            }
        } else {
            mMap!!.isBuildingsEnabled = true
        }


        //Directions
        img_direction?.setOnClickListener {
            showDirectionDialog()
        }

    }

    private fun showDirectionDialog() {
        var dialog = Dialog(this@MapsActivity, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.dialog_search_direction)

        //get filter location nearest you
        val filter = AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
            .build()

        //show all search address
        placesAdapter =
            PlacesAdapter(this, android.R.layout.simple_list_item_1, mGeoDataClient, filter, LAT_LNG_BOUNDS!!)

        //origin
        dialog.place_Autocomplete_StartingPoint.setAdapter(placesAdapter)
        dialog.place_Autocomplete_StartingPoint.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard()
            val item = placesAdapter.getItem(position)
            val placeId = item?.placeId
            val originPrimaryText = item?.getPrimaryText(null)

            Log.i("Autocomplete", "Autocomplete item selected: $originPrimaryText")


            val placeResult = mGeoDataClient.getPlaceById(placeId)
            placeResult.addOnCompleteListener { task ->
                val places = task.result
                val place = places!!.get(0)

                isAutoCompleteLocation = true
                latLngOrigin = place.latLng
//                originToMap()

                places.release()
            }

            Toast.makeText(
                applicationContext, "Origin: $originPrimaryText",
                Toast.LENGTH_SHORT
            ).show()
        }

        //destination
        dialog.place_Autocomplete_Destination.setAdapter(placesAdapter)
        dialog.place_Autocomplete_Destination.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard()

            val item = placesAdapter.getItem(position)
            val placeId = item?.placeId
            val destinationPrimaryText = item?.getPrimaryText(null)

            Log.i("Autocomplete", "Autocomplete item selected: $destinationPrimaryText")

            val placeResult = mGeoDataClient.getPlaceById(placeId)
            placeResult.addOnCompleteListener { task ->
                val places = task.result
                val place = places!!.get(0)

                isAutoCompleteLocation = true
                latLngDestination = place.latLng
//                destinationToMap()

                places.release()
            }

            Toast.makeText(
                applicationContext, "Destination: $destinationPrimaryText",
                Toast.LENGTH_SHORT
            ).show()
        }


        //Search Place
        dialog.btn_Go.setOnClickListener {
            val origin: String = dialog.place_Autocomplete_StartingPoint.text.toString()
            val destination: String = dialog.place_Autocomplete_Destination.text.toString()

            if (origin.isEmpty()) {
                Toast.makeText(this, "Please enter Origin address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (destination.isEmpty()) {
                Toast.makeText(this, "Please enter Destination address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                DirectionFinder(this, origin, destination).execute()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            dialog.dismiss()
        }

        //Cancel
        dialog.btn_Cancel.setOnClickListener {
            mMap.clear()
            dialog.dismiss()
        }

        dialog.show()
    }

    /** To hide Keyboard */
    private fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Camera Move */
    private fun originToMap() {
        mMap.clear()
        mMap.apply {
            addMarker(MarkerOptions()
                .position(latLngOrigin)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            moveCamera(CameraUpdateFactory.newLatLng(latLngOrigin))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, cameraZoomDirection))
        }

    }

    private fun destinationToMap() {

        mMap.apply {
            addMarker(MarkerOptions()
                .position(latLngDestination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
            moveCamera(CameraUpdateFactory.newLatLng(latLngDestination))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLngDestination, cameraZoomDirection))
        }

    }

    /**---------------------Draw Router----------Direction-----------------------------*/

    override fun onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(
            this, "Please wait.",
            "Finding direction..!", true
        )

        if (originMarkers != null) {
            for (marker in originMarkers!!) {
                marker.remove()
            }
        }

        if (destinationMarkers != null) {
            for (marker in destinationMarkers!!) {
                marker.remove()
            }
        }

        if (polylinePaths != null) {
            for (polyline in polylinePaths!!) {
                polyline.remove()
            }
        }
    }

    override fun onDirectionFinderSuccess(routes: List<Route>) {
        progressDialog?.dismiss()
        polylinePaths = ArrayList()
        originMarkers = ArrayList()
        destinationMarkers = ArrayList()

        for (route in routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, cameraZoomDirection))
            (findViewById<TextView>(R.id.tvDuration)).text = route.duration?.text
            (findViewById<TextView>(R.id.tvDistance)).text = route.distance?.text

            originMarkers?.add(
                mMap.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .title(route.startAddress)
                        .position(route.startLocation!!)
                )
            )

            destinationMarkers?.add(
                mMap.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .title(route.endAddress)
                        .position(route.endLocation!!)
                )
            )

            val polylineOptions = PolylineOptions().geodesic(true).color(Color.BLUE).width(12f)

            for (i in 0 until route.points!!.size)
                polylineOptions.add(route.points!![i])

            polylinePaths?.add(mMap.addPolyline(polylineOptions))
        }
    }
}
