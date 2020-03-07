package com.kadabra.agent.courier

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker.getPlace
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.firebase.LatLngInterpolator
import com.kadabra.agent.firebase.LocationHelper
import com.kadabra.agent.firebase.MarkerAnimation
import com.kadabra.agent.model.Courier
import com.kadabra.agent.model.Stop
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.cartello.Utilities.Base.BaseFragment
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs


class CourierFragment : BaseFragment(), IBottomSheetCallback, OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {


    //region Members
    private lateinit var currentView: View
    private lateinit var map: GoogleMap
    private lateinit var ivSearchMarker: ImageView
    private lateinit var btnConfirmLocation: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var firstTimeFlag = true
    private var listener: IBottomSheetCallback? = null
    var searchMode = false
    var isMoving = false

    private var currentMarker: Marker? = null
    private var stop: Stop = Stop()
    var address = ""
    var city = ""
    var geoCoder: Geocoder? = null
    var addresList: List<Address>? = null
    private lateinit var selectedLatLng: LatLng
    var markers = HashMap<String, Courier>()
    private val TAG = CourierFragment::class.java!!.simpleName

    //endregion

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3


    }

    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        LocationHelper.shared.initializeLocation(activity!!)
//        requestPermission()
        if (!checkPermissions()) {
            requestPermissions()
        }
        FirebaseManager.setUpFirebase()

    }

    private fun requestPermission() {
        if (NetworkManager().isNetworkAvailable(context!!)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
            if (checkPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                fusedLocationClient?.lastLocation.addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare
                    // situations this can be null.
                    if (location == null) {
                        //no data

                    } else location.apply {
                        // Handle location object
                        Log.e("LOG", location.toString())
                        AppConstants.CurrentLocation = location
                    }
                }
            }
        } else {
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            val dialog = androidx.appcompat.app.AlertDialog.Builder(activity!!)
                .setTitle(getString(R.string.Permission))
                .setMessage(getString(R.string.error_location_permission_required))
                .setPositiveButton(getString(R.string.ok)) { id, v ->
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )

                }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .create()
            dialog.show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_courier, container, false)
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }



    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
//        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)


        if (searchMode) {

            map.clear()

            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE

            if (lastLocation != null) {
                val googlePlex = CameraPosition.builder()
                    .target(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
                    .zoom(12f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()

                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(googlePlex),
                    1000,
                    null
                )

                selectedLatLng = map.cameraPosition.target
            }

            map.setOnCameraIdleListener {

                //update the current location on camera view change
                selectedLatLng = map.cameraPosition.target
            }

        }
        // show all couriers on map for tracking
        else {

            if (!AppConstants.isMoving)
                loadAllCouriersFromFB()
        }

        //couriers points
//            loadAllCouriers()  //this from db

    }

    private fun loadAllCouriers() {
        var marker: Marker? = null
        AppConstants.ALL_COURIERS.forEach {
            var latlng = LatLng(30.0163243, 30.9990016)
            var latlngNew = getRandomLocation(latlng, 13254)

//            if (it.CourierId == 2) {

            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latlngNew.latitude, latlngNew.longitude))
                    .title(it.name)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                        )
                    )
            )

//            } else {
//                marker = map.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(latlngNew.latitude, latlngNew.longitude))
//                        .title(it.PaymentName)
//
//                )
//            }
            markers[marker!!.id] = it
        }


    }


    private fun loadAllCouriersFromFB() {
        var marker: Marker? = null
        var markersList: ArrayList<Marker>? = ArrayList()
        var latLngList: ArrayList<LatLng>? = ArrayList()
        var currentLatLng: LatLng? = null
        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getAllCouriers { success, data ->
                if (success) {
                    map.clear()
                    data!!.forEach {
                        currentLatLng =
                            LatLng(it.location.lat.toDouble(), it.location.long.toDouble())

                        latLngList!!.add(currentLatLng!!)

                        marker = map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        it.location.lat.toDouble(),
                                        it.location.long.toDouble()
                                    )
                                )
                                .title(it.name)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        BitmapFactory.decodeResource(
                                            resources,
                                            R.mipmap.ic_launcher
                                        )
                                    )
                                )
                        )
                        markersList!!.add(marker!!)
                        markers[marker!!.id] = it

                    }
                }


                val googlePlex = CameraPosition.builder()
                    .target(currentLatLng)
                    .zoom(14f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()

//                map.animateCamera(
//                    CameraUpdateFactory.newCameraPosition(googlePlex),
//                    1000,
//                    null
//                )


                //get all the couriers on the map
                if (markersList!!.size > 0)
                    prepareGetAllCourierView( markersList)

            }

        } else
            Alert.showMessage(context!!, getString(R.string.no_internet))

    }


    private fun prepareGetAllCourierView(
        markersList: ArrayList<Marker>
    ) {

        var builder = LatLngBounds.builder()
        markersList.forEach { marker ->
            builder.include(marker.position)

        }
        var bounds = builder.build()
        var padding = 0 // offset from edges of the map in pixels
        var cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.setMaxZoomPreference(10.0F)
        map.animateCamera(cu)


    }


    override fun onMarkerClick(courierMarker: Marker?): Boolean {

        var currentCourier = markers[courierMarker!!.id]
        AppConstants.isMoving = true
        getCurrentCourierLocation(currentCourier!!.CourierId!!.toInt())
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    setUpMap()

                    Toast.makeText(
                        context!!,
                        getString(R.string.permission_denied_explanation),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }// other 'case' lines to check for other
        // permissions this app might request.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val place = getPlace(context, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions())
            requestPermissions()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    //region Helper Function
    private fun init() {
        ivSearchMarker = currentView.findViewById(R.id.ivSearchMarker)
        btnConfirmLocation = currentView.findViewById(R.id.btnConfirmLocation)



        if (searchMode) {
            map.clear()
            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE
        } else {
            btnConfirmLocation.visibility = View.GONE
            ivSearchMarker.visibility = View.GONE
        }

        btnConfirmLocation.setOnClickListener {

            searchMode = false

            getFullLocationData(selectedLatLng.latitude, selectedLatLng.longitude)

        }

//        fab.setOnClickListener {
//            loadPlacePicker()
//        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        //prepare for update the current location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                //todo move the marker position
//                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN

        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }


    }

    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        //change marker icon
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            )
        )

        //show address of the current location
//        val titleStr = getAddress(location)  // add these two lines
//        markerOptions.title(titleStr)
        // 2
        map.clear()

        map.addMarker(markerOptions)

        val zoom = map.cameraPosition.zoom

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    //get the current location address
    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(context)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    //update the current location
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(activity!!)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        activity!!,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // places
    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(activity!!), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private fun getFullLocationData(lat: Double, lng: Double): Stop {
        geoCoder = Geocoder(context!!, Locale.getDefault())
        try {
            addresList = geoCoder!!.getFromLocation(lat, lng, 1)
            address = addresList!![0].getAddressLine(0)
            city = addresList!![0].locality?.toString() ?: ""
            stop.Latitude = lat
            stop.Longitude = lng
            stop.city = city
            stop.country = addresList!![0].countryName?.toString() ?: ""
            stop.address = address
            stop.state = addresList!![0].adminArea?.toString() ?: ""
            stop.postalCode = addresList!![0].postalCode?.toString() ?: ""
            stop.knownName = addresList!![0].featureName?.toString() ?: ""

            if (stop.knownName.isNotEmpty() && stop.city.isNullOrEmpty())
                stop.StopName = stop.knownName
            else if (stop.knownName.isNullOrEmpty() && stop.city.isNullOrEmpty() && stop.address.isNotEmpty())
                stop.StopName = stop.address

            AppConstants.CurrentTempStop = stop
            btnConfirmLocation.visibility = View.GONE
            ivSearchMarker.visibility = View.GONE
            listener!!.onBottomSheetSelectedItem(9) //got to task new fragment with stop data

        } catch (ex: Exception) {
            print(ex.message)
        }


        return stop
    }

    fun getRandomLocation(point: LatLng, radius: Int): LatLng {

        val randomPoints = java.util.ArrayList<LatLng>()
        val randomDistances = java.util.ArrayList<Float>()
        val myLocation = Location("")
        myLocation.latitude = point.latitude
        myLocation.longitude = point.longitude

        //This is to generate 10 random points
        for (i in 0..9) {
            val x0 = point.latitude
            val y0 = point.longitude

            val random = Random()

            // Convert radius from meters to degrees
            val radiusInDegrees = (radius / 111320f).toDouble()

            val u = random.nextDouble()
            val v = random.nextDouble()
            val w = radiusInDegrees * Math.sqrt(u)
            val t = 2.0 * Math.PI * v
            val x = w * Math.cos(t)
            val y = w * Math.sin(t)

            // Adjust the x-coordinate for the shrinking of the east-west distances
            val new_x = x / Math.cos(Math.toRadians(y0))

            val foundLatitude = new_x + x0
            val foundLongitude = y + y0
            val randomLatLng = LatLng(foundLatitude, foundLongitude)
            randomPoints.add(randomLatLng)
            val l1 = Location("")
            l1.latitude = randomLatLng.latitude
            l1.longitude = randomLatLng.longitude
            randomDistances.add(l1.distanceTo(myLocation))
        }
        //Get nearest point to the centre
        val indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances))
        return randomPoints[indexOfNearestPointToCentre]
    }

    private fun getCurrentCourierLocation(courierId: Int) {

        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getCurrentCourierLocation(courierId.toString()) { success, location ->
                if (success) {
//                    map.clear()

//                    placeMarkerOnMap(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
//                    showMarker(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
                    if (firstTimeFlag && map != null) {
                        animateCamera(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
//                        moveCamera(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location!!.lat.toDouble(),
                                    location!!.long.toDouble()
                                ), 14F
                            )
                        )
                        firstTimeFlag = false

                    } else {

                        var latLng = LatLng(location!!.lat.toDouble(), location!!.long.toDouble())

                        if (currentMarker != null) {
                            currentMarker!!.remove()
                        }

                        //Place current location marker

                        val markerOptions = MarkerOptions().position(latLng)
                        //change marker icon
                        markerOptions.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                            )
                        )
                        currentMarker = map.addMarker(markerOptions)

                        //move map camera
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
                    }
//                        showMarker(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))

                } else {


                }
            }
        } else {
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }

    }

    private fun animateCamera(latLng: LatLng) {

        val zoom = map.cameraPosition.zoom
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    latLng,
                    zoom
                )
            )
        )
    }

    private fun moveCamera(latLng: LatLng) {

        val zoom = map.cameraPosition.zoom
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    latLng,
                    20F
                )
            )
        )
    }

    private fun showMarker(latLng: LatLng) {
        if (currentMarker == null) {
            val markerOptions = MarkerOptions().position(latLng)
            //change marker icon
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                )
            )
            currentMarker = map.addMarker(markerOptions)

//            map.animateCamera(CameraUpdateFactory.zoomTo(18F))

        } else {
//            MarkerAnimation.animateMarkerToGB(
//                currentMarker,
//                latLng,
//                LatLngInterpolator.Spherical()
//            )

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
//            animateCamera(latLng)
//            moveCamera(latLng)
//            map.animateCamera(CameraUpdateFactory.zoomTo(18F))
//
        }
    }


    private fun checkPermission(vararg perm: String): Boolean {
        val havePermissions = perm.toList().all {
            ActivityCompat.checkSelfPermission(context!!, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if (perm.toList().any {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity!!,
                        it
                    )
                }
            ) {

                val dialog = androidx.appcompat.app.AlertDialog.Builder(activity!!)
                    .setTitle(getString(R.string.Permission))
                    .setMessage(getString(R.string.error_location_permission_required))
                    .setPositiveButton(getString(R.string.ok)) { id, v ->
                        ActivityCompat.requestPermissions(
                            activity!!, perm, LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                    .create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    activity!!, perm,
                    LOCATION_PERMISSION_REQUEST_CODE
                )

            }
            return false
        } else {
            var s = ""
        }
        return true
    }
//endregion
}
