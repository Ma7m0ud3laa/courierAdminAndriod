package com.twoam.agent.courier

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker.getPlace
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.twoam.Networking.NetworkManager
import com.twoam.agent.R
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.firebase.FirebaseManager
import com.twoam.agent.model.Courier
import com.twoam.agent.model.Stop
import com.twoam.agent.utilities.Alert
import com.twoam.agent.utilities.AppConstants
import com.twoam.cartello.Utilities.Base.BaseFragment
import java.io.IOException
import java.lang.Exception
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CourierFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CourierFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
    private var param1: String? = null
    private var param2: String? = null
    private var listener: IBottomSheetCallback? = null
    var searchMode = false
    private var currentMarker: Marker? = null
    private var stop: Stop = Stop()
    var address = ""
    var city = ""
    var geoCoder: Geocoder? = null
    var addresList: List<Address>? = null
    private lateinit var selectedLatLng: LatLng
    var markers = HashMap<String, Courier>()


    //endregion

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3


    }

    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        FirebaseManager.setUpFirebase()
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

        if (searchMode) {

            map.clear()

            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE


            //set the map in  the current position
            if (lastLocation == null) {
                val googlePlex = CameraPosition.builder()
                    .target(LatLng(30.0163243, 30.9990016))
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

            } else {
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
            val googlePlex = CameraPosition.builder()
                .target(LatLng(30.0163243, 30.9990016))
                .zoom(12f)
                .bearing(0f)
                .tilt(45f)
                .build()


            map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)
            //couriers points
//            loadAllCouriers()  //this from db

            loadAllCouriersFromFB()
        }

    }

    private fun loadAllCouriers() {
        var marker: Marker? = null
        AppConstants.ALL_COURIERS.forEach {
            var latlng = LatLng(30.0163243, 30.9990016)
            var latlngNew = getRandomLocation(latlng, 13254)

            if (it.CourierId == 2) {

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

            } else {
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latlngNew.latitude, latlngNew.longitude))
                        .title(it.name)

                )
            }
            markers[marker!!.id] = it
        }


    }


    private fun loadAllCouriersFromFB() {
        var marker: Marker? = null
        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getAllCouriers { success, data ->
                if (success) {
                    data!!.forEach {

                        if (it.CourierId == 2) {

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

                        } else {
                            marker = map.addMarker(
                                MarkerOptions()
                                    .position(
                                        LatLng(
                                            it.location.lat.toDouble(),
                                            it.location.long.toDouble()
                                        )
                                    )
                                    .title(it.name)

                            )
                        }
                        markers[marker!!.id] = it
                    }
                }
            }
        } else
            Alert.showMessage(context!!, getString(R.string.no_internet))

    }


    override fun onMarkerClick(courierMarker: Marker?): Boolean {

        var currentCourier = markers[courierMarker!!.id]

        getCurrentCourierLocation(currentCourier!!.CourierId!!.toInt())
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    setUpMap()
                }
                return
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
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // Permission has already been granted
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN //more map details
        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->

            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)

            }
        }
        return

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
        map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
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
        map.clear()

        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getCurrentCourierLocation(courierId.toString(),
                object : FirebaseManager.IFbOperation {
                    override fun onSuccess(code: Int) {

                    }

                    override fun onFailure(message: String) {

                    }
                })
        } else {
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }

    }

//endregion
}
