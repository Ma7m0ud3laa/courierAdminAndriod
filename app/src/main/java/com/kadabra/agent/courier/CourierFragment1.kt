package com.kadabra.agent.courier

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.location.places.ui.PlacePicker.getPlace
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
//import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.google.android.material.snackbar.Snackbar
import com.kadabra.Networking.NetworkManager
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.BuildConfig
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.direction.TaskLoadedCallback
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.model.Courier
import com.kadabra.agent.model.Stop
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import java.io.IOException
import java.lang.Exception
import java.util.*


class CourierFragment1 : BaseFragment(), IBottomSheetCallback, OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, TaskLoadedCallback {


    //region Members
    private lateinit var currentView: View
    private lateinit var mMap: GoogleMap
    private lateinit var ivBack: ImageView
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
    private var isFirstTime = true
    private var currentMarker: Marker? = null
    private var stop: Stop = Stop()
    var address = ""
    var city = ""
    var geoCoder: Geocoder? = null
    var addresList: List<Address>? = null
    private lateinit var selectedLatLng: LatLng
    var markers = HashMap<String, Courier>()
    private val TAG = CourierFragment1::class.java!!.simpleName
    private val COLORS: IntArray = intArrayOf(
        R.color.colorPrimary,
        R.color.colorAccent,
        R.color.colorApp,
        R.color.colorLabel,
        R.color.primary_dark_material_light
    )

    private lateinit var polylines: List<Polyline>
    private var currentPolyline: Polyline? = null
    private var placesClient: PlacesClient? = null
    private var materialSearchBar: MaterialSearchBar? = null
    private var predictionList: List<AutocompletePrediction>? = null
    private val DEFAULT_ZOOM = 15f
    private var mapView: View? = null
    //endregion

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3


    }

    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        if (!checkPermissions()) {
//            requestPermissions()
//        }

        FirebaseManager.setUpFirebase()

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
            Snackbar.make(
                currentView.findViewById(R.id.rlParent),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                .show()

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

        var mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapView = mapFragment!!.view



        Places.initialize(context!!, getString(R.string.google_maps_key))
        placesClient = Places.createClient(context!!)
        val token = AutocompleteSessionToken.newInstance()


        materialSearchBar!!.setOnSearchActionListener(object :
            MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {

            }

            override fun onSearchConfirmed(text: CharSequence) {
                activity!!.startSearch(text.toString(), true, null, true)
            }

            override fun onButtonClicked(buttonCode: Int) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar!!.disableSearch()
                }
            }
        })

        materialSearchBar!!.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val predictionsRequest = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token!!)
                    .setQuery(s.toString())
                    .build()
                placesClient!!.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val predictionsResponse = task.result
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.autocompletePredictions
                                val suggestionsList = ArrayList<String>()
                                for (i in predictionList!!.indices) {
                                    val prediction = predictionList!!.get(i)
                                    suggestionsList.add(prediction.getFullText(null).toString())
                                }
                                materialSearchBar!!.updateLastSuggestions(suggestionsList)
                                if (!materialSearchBar!!.isSuggestionsVisible) {
                                    materialSearchBar!!.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful")
                        }
                    }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        materialSearchBar!!.setSuggstionsClickListener(object :
            SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View) {
                if (position >= predictionList!!.size) {
                    return
                }
                val selectedPrediction = predictionList!![position]
                val suggestion = materialSearchBar!!.lastSuggestions[position].toString()
                materialSearchBar!!.text = suggestion

                Handler().postDelayed({ materialSearchBar!!.clearSuggestions() }, 1000)
                val imm = context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(
                    materialSearchBar!!.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
                val placeId = selectedPrediction.placeId
                val placeFields = listOf(Place.Field.LAT_LNG)

                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient!!.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        if(place.name!=null)
                            Log.i("mytag", "Place found: " + place.name)
                        else
                            Log.i("mytag", "Place found: " +"No Name is found")

                        val latLngOfPlace = place.latLng
                        if (latLngOfPlace != null) {
                            ivSearchMarker.visibility = View.VISIBLE
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    DEFAULT_ZOOM
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            e.printStackTrace()
                            val statusCode = e.statusCode
                            Log.i("mytag", "place not found: " + e.message)
                            Log.i("mytag", "status code: $statusCode")
                        }
                    }
            }

            override fun OnItemDeleteListener(position: Int, v: View) {

            }
        })
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
//        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        if (mapView != null && mapView!!.findViewById<View>(Integer.parseInt("1")) != null) {
            val locationButton =
                (mapView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(
                    Integer.parseInt("2")
                )
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 180)
        }


        if (searchMode) {

            mMap.clear()

            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE

            if (lastLocation != null) {
                val googlePlex = CameraPosition.builder()
                    .target(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
                    .zoom(12f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()

                mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(googlePlex),
                    1000,
                    null
                )

                selectedLatLng = mMap.cameraPosition.target
            }

            mMap.setOnCameraIdleListener {

                //update the current location on camera view change
                selectedLatLng = mMap.cameraPosition.target
            }

        }
        // show all couriers on mMap for tracking
        else {

            if (!AppConstants.isMoving)
                loadAllCouriersFromFB()
        }

        //couriers points
//            loadAllCouriers()  //this from db

        mMap.setOnMyLocationButtonClickListener {
            if (materialSearchBar!!.isSuggestionsVisible)
                materialSearchBar!!.clearSuggestions()
            if (materialSearchBar!!.isSearchEnabled)
                materialSearchBar!!.disableSearch()
            false
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
                    mMap.clear()
                    data!!.forEach {
                        currentLatLng =
                            LatLng(it.location.lat.toDouble(), it.location.long.toDouble())

                        latLngList!!.add(currentLatLng!!)

                        marker = mMap.addMarker(
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

//                mMap.animateCamera(
//                    CameraUpdateFactory.newCameraPosition(googlePlex),
//                    1000,
//                    null
//                )


                //get all the couriers on the mMap
                if (markersList!!.size > 0)
                    prepareGetAllCourierView(markersList)

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
        var padding = 0 // offset from edges of the mMap in pixels
        var cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.setMaxZoomPreference(18.0F)
        mMap.animateCamera(cu)


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
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == AppConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
//                LocationUpdatesService.shared!!.requestLocationUpdates()
            } else {
                // Permission denied.

                Snackbar.make(
                    currentView.findViewById(R.id.rlParent),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CHECK_SETTINGS) {
//            if (resultCode == Activity.RESULT_OK) {
//                locationUpdateState = true
//                startLocationUpdates()
//            }
//        }
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == AppCompatActivity.RESULT_OK) {
////                val place = getPlace(context, data)
////                var addressText = place.name.toString()
////                addressText += "\n" + place.address.toString()
////
////                placeMarkerOnMap(place.latLng)
//            }
//        }
//    }

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
        ivBack = currentView.findViewById(R.id.ivBack)
        ivSearchMarker = currentView.findViewById(R.id.ivSearchMarker)
        btnConfirmLocation = currentView.findViewById(R.id.btnConfirmLocation)
        materialSearchBar = currentView.findViewById(R.id.searchBar)



        if (searchMode) {
//            mMap.clear()
            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE
        } else {
            btnConfirmLocation.visibility = View.GONE
            ivSearchMarker.visibility = View.GONE
        }

        ivBack.setOnClickListener {
            if (searchMode)
                listener?.onBottomSheetSelectedItem(6)
            else
                listener?.onBottomSheetSelectedItem(2)
        }
        btnConfirmLocation.setOnClickListener {


            getFullLocationData(selectedLatLng.latitude, selectedLatLng.longitude)

        }


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


    //update the current location
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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


    private fun getFullLocationData(lat: Double, lng: Double): Stop {
        geoCoder = Geocoder(context!!, Locale.getDefault())
        try {
            stop = Stop()
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
            btnConfirmLocation.visibility = View.GONE
            ivSearchMarker.visibility = View.GONE
            listener!!.onBottomSheetSelectedItem(9) //got to task new fragment with stop data

        } catch (ex: Exception) {
            print(ex.message)
        }


        return stop
    }


    private fun getCurrentCourierLocation(courierId: Int) {

        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getCurrentCourierLocation(courierId.toString()) { success, location ->
                if (success) {
//                    mMap.clear()

//                    placeMarkerOnMap(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
//                    showMarker(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
                    if (firstTimeFlag && mMap != null) {
                        animateCamera(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
//                        moveCamera(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
                        mMap.moveCamera(
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
                        currentMarker = mMap.addMarker(markerOptions)

                        //move mMap camera
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
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

        val zoom = mMap.cameraPosition.zoom
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    latLng,
                    zoom
                )
            )
        )
    }

    private fun moveCamera(latLng: LatLng) {

        val zoom = mMap.cameraPosition.zoom
        mMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    latLng,
                    20F
                )
            )
        )
    }


    override fun onTaskDone(vararg values: Any?) {
        if (currentPolyline != null)
            currentPolyline!!.remove()
        currentPolyline = mMap.addPolyline(values[0] as PolylineOptions)

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentPolyline!!.points.get(0).latitude,
                    currentPolyline!!.points.get(0).longitude
                ), 8f
            )
        )
    }

    //endregion
}