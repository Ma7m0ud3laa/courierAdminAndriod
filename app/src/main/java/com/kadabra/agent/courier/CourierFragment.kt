package com.kadabra.agent.courier


import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.akexorcist.googledirection.BuildConfig
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.model.Leg
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.ui.IconGenerator
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.kadabra.Networking.NetworkManager
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.adapter.CustomInfoWindowAdapter
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.direction.TaskLoadedCallback
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.model.*
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.fragment_courier.*
import java.util.*


class CourierFragment : BaseFragment(), IBottomSheetCallback, OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, TaskLoadedCallback, View.OnClickListener,
    GoogleMap.OnPolylineClickListener {


    //region Members
    private lateinit var currentView: View
    private lateinit var mMap: GoogleMap
    private lateinit var ivBack: ImageView
    private lateinit var ivSearchMarker: ImageView
    private lateinit var btnConfirmLocation: Button
    private var currentSelectedCourier = Courier()
    var suggestionsList = ArrayList<String>()
    private var firstTimeFlag = true
    private var listener: IBottomSheetCallback? = null
    var searchMode = false
    private var stop: Stop = Stop()
    var address = ""
    var city = ""
    var geoCoder: Geocoder? = null
    var addresList: List<Address>? = null
    private lateinit var selectedLatLng: LatLng
    var markers = WeakHashMap<String, Courier>()
    var currentSelectedMarker: Marker? = null
    private val TAG = CourierFragment::class.java!!.simpleName
    private var placesClient: PlacesClient? = null
    private var materialSearchBar: MaterialSearchBar? = null
    private var predictionList: List<AutocompletePrediction>? = null
    private var DEFAULT_ZOOM = 15f
    private var MAX_ZOOM = 20F
    private var mapView: View? = null
    private var currentSelectedCourierId = 0
    var directionMode = false;
    private lateinit var polylines: List<Polyline>
    private var currentPolyline: Polyline? = null
    private var isFirstTime = true
    private lateinit var destination: LatLng
    private var currentMarker: Marker? = null
    private var mGeoApiContext: GeoApiContext? = null
    private var mPolyLinesData: ArrayList<PolylineData> = ArrayList()
    private var mNewPolyLinesData: ArrayList<NewPolylineData> = ArrayList()
    private val mTripMarkers = ArrayList<Marker>()
    private var mSelectedMarker: Marker? = null
    private var totalKilometers: Float = 0F
    private var acceptedTaskslist = ArrayList<Task>()
    private lateinit var polyline: Polyline
    var isACcepted = false
    private lateinit var directionResult: DirectionsResult
    private lateinit var rlBottom: RelativeLayout
    private lateinit var tvExpectedTime: TextView
    private lateinit var tvExpectedDistance: TextView
    private var currentCountry = ""
    private var countrygeoCoder: Geocoder? = null
    private lateinit var locale: Locale
    private var couriersList = ArrayList<Courier>()
    var searchOnCourier = false
    var firstStop = Stop()
    var lastStop = Stop()
    var waypoints: ArrayList<LatLng> = ArrayList()
    //endregion


    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppConstants.isMoving = false //required for reget all the couriers data from firebase


        currentCountry = getUserCountry(context!!)!!
        countrygeoCoder = Geocoder(context!!, Locale.forLanguageTag(currentCountry))
        locale = Locale("", currentCountry)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_courier, container, false)
        ivBack = currentView.findViewById(R.id.ivBack)
        ivSearchMarker = currentView.findViewById(R.id.ivSearchMarker)
        btnConfirmLocation = currentView.findViewById(R.id.btnConfirmLocation)
        rlBottom = currentView.findViewById(R.id.rlBottom)
        tvExpectedTime = currentView.findViewById(R.id.tvExpectedTime)
        tvExpectedDistance = currentView.findViewById(R.id.tvExpectedDistance)


        materialSearchBar = currentView.findViewById(R.id.searchBar)
        ivBack.setOnClickListener(this)
        btnConfirmLocation.setOnClickListener(this)
//        polylines = ArrayList()
        //////////////////////////////////////////////////////////////
        var mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapView = mapFragment!!.view

        //direction
        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build()
        }

        Places.initialize(context!!, context!!.getString(R.string.google_map_key))
        placesClient = Places.createClient(context!!)
        val token = AutocompleteSessionToken.newInstance()

        if (searchMode) {
            materialSearchBar!!.setPlaceHolder("Search a place")
            suggestionsList.clear()
            couriersList.clear()
            materialSearchBar!!.updateLastSuggestions(suggestionsList)
        } else if (directionMode) {
            materialSearchBar!!.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
        } else if (searchOnCourier)
            materialSearchBar!!.setPlaceHolder("Find a courier")

        materialSearchBar!!.setCardViewElevation(10)
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
                    suggestionsList.clear()
                    couriersList.clear()
                    materialSearchBar!!.updateLastSuggestions(suggestionsList)
                    materialSearchBar!!.disableSearch()
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar!!.disableSearch()
//                    materialSearchBar.hideSuggestionsList()
                } else { //act like press on back icon
                    if (searchMode)
                        listener?.onBottomSheetSelectedItem(6)
                    else
                        listener?.onBottomSheetSelectedItem(2)
                }
            }
        })

        materialSearchBar!!.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // region for device for datetime
                if (searchMode) {
                    val predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setCountry(currentCountry)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build()
                    placesClient!!.findAutocompletePredictions(predictionsRequest)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val predictionsResponse = task.result
                                if (predictionsResponse != null) {
                                    predictionList = predictionsResponse.autocompletePredictions
                                    suggestionsList = ArrayList()
                                    for (i in predictionList!!.indices) {
                                        val prediction = predictionList!![i]
                                        suggestionsList.add(prediction.getFullText(null).toString())
                                    }
                                    materialSearchBar!!.updateLastSuggestions(suggestionsList)
                                    if (!materialSearchBar!!.isSuggestionsVisible) {
                                        materialSearchBar!!.showSuggestionsList()
                                    }
                                }
                            } else {
                                Log.i(TAG, "prediction fetching task unsuccessful")
                            }
                        }
                } else if (searchOnCourier) { //direction search for courier
//                    suggestionsList.clear()
                    suggestionsList = ArrayList()
                    if (couriersList.size > 0) {
                        couriersList.forEach { suggestionsList.add(it.name) }
                        materialSearchBar!!.updateLastSuggestions(suggestionsList)

                        if (!materialSearchBar!!.isSuggestionsVisible) {
                            materialSearchBar!!.showSuggestionsList()
                        }
                        print(couriersList)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        materialSearchBar!!.setSuggstionsClickListener(object :
            SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View) {

                if (searchMode) {
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
                            if (place.name != null)
                                Log.i("mytag", "Place found: " + place.name)
                            else
                                Log.i("mytag", "Place found: " + "No Name is found")

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
                } else if (searchOnCourier) {
                    if (position >= couriersList!!.size) {
                        return
                    }
                    val selectedCourier = couriersList!![position]
                    val suggestion = materialSearchBar!!.lastSuggestions[position].toString()
                    materialSearchBar!!.text = suggestion

                    Handler().postDelayed({ materialSearchBar!!.clearSuggestions() }, 1000)
                    val imm = context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm?.hideSoftInputFromWindow(
                        materialSearchBar!!.windowToken,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
//                  var currentCourier=couriersList.find { it.name==selectedCourier.name }


                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                selectedCourier!!.location.lat.toDouble(),
                                selectedCourier!!.location.long.toDouble()
                            )
                            ,
                            DEFAULT_ZOOM
                        )
                    )
                }
            }

            override fun OnItemDeleteListener(position: Int, v: View) {
                if (suggestionsList.size > 0) {
                    suggestionsList.removeAt(position)
                    materialSearchBar!!.updateLastSuggestions(suggestionsList)
                }

            }
        })

        ///////////////////////////////////////////////////


        if (searchMode) {
            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE
        } else {
            btnConfirmLocation.visibility = View.GONE
            ivSearchMarker.visibility = View.GONE
        }

        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ivBack -> {
                if (searchMode)
                    listener?.onBottomSheetSelectedItem(6)
                else if(directionMode)
                {
                    directionMode=false
                    listener?.onBottomSheetSelectedItem(6)
                }
                else
                    listener?.onBottomSheetSelectedItem(2)
            }
            R.id.btnConfirmLocation -> {
                getFullLocationData(selectedLatLng.latitude, selectedLatLng.longitude)
            }
        }
    }

    override fun onPolylineClick(p0: Polyline?) {
        var index = 0
        for (polylineData in mPolyLinesData) {
            index++
            Log.d(
                TAG,
                "onPolylineClick: toString: $polylineData"
            )
            if (polyline!!.id == polylineData.polyline.id) {
                polylineData.polyline.color =
                    ContextCompat.getColor(context!!, R.color.primary_dark)
                polylineData.polyline.setZIndex(1F)
                val endLocation =
                    LatLng(
                        polylineData.leg.endLocation.lat,
                        polylineData.leg.endLocation.lng
                    )

                setTripDirectionData(polylineData)
                var pickUpStop = AppConstants.CurrentSelectedTask.stopsmodel.first()
                val marker: Marker = mMap.addMarker(
                    MarkerOptions()
                        .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_location))
                        .position(endLocation)
                        .title("[" + getString(R.string.trip) + " " + index + "] - " + pickUpStop.StopName)
                        .snippet(
                            getString(R.string.duration) + " " + polylineData.leg.duration + " " + (getString(
                                R.string.distance
                            ) + " " + polylineData.leg.distance)
                        )


                )
                mTripMarkers.add(marker)
//                marker.showInfoWindow()
            } else {
                polylineData.polyline.color =
                    ContextCompat.getColor(context!!, R.color.colorPrimary)
                polylineData.polyline.setZIndex(0F)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.clear()
        mMap.setOnMarkerClickListener(this)
        mMap.setOnPolylineClickListener(this)

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


        var adresses = countrygeoCoder?.getFromLocationName(locale.displayCountry, 1)
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(adresses?.get(0)?.latitude!!, adresses?.get(0)?.longitude!!),
                8f
            )
        )

        if (searchMode) {


            btnConfirmLocation.visibility = View.VISIBLE
            ivSearchMarker.visibility = View.VISIBLE

//            selectedLatLng = mMap.cameraPosition.target

            mMap.setOnCameraIdleListener {

                //update the current location on camera view change
                selectedLatLng = mMap.cameraPosition.target
            }

        } else if (mMap != null && directionMode) {

//            requestDirection(AppConstants.CurrentSelectedTask)
            getTAskDirection(AppConstants.CurrentSelectedTask)
        }
        // show all couriers on mMap for tracking
        else {

            if (!AppConstants.isMoving)
                loadAllCouriersFromFB()
        }



        mMap.setOnMyLocationButtonClickListener {

            false
        }

    }

    override fun onMarkerClick(courierMarker: Marker?): Boolean {
        var done = false
        if (!searchMode && DEFAULT_ZOOM < MAX_ZOOM) {
            try {
                var currentCourier = courierMarker?.tag as Courier
                if (currentCourier != null && currentCourier.CourierId!! > 0) {
                    AppConstants.isMoving = true
                    if (currentSelectedCourierId != currentCourier.CourierId) {
                        firstTimeFlag = true
                        getCurrentCourierLocation(currentCourier.CourierId!!)
                    }
                }
            } catch (ex: java.lang.Exception) {
                Log.e(TAG, ex.message)
            }
            done = true
        }

        if (directionMode)
            done = false
        return done
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
                    mMap.clear()
                    currentSelectedCourierId = courierId

                    if (firstTimeFlag && mMap != null) {
                        animateCamera(LatLng(location!!.lat.toDouble(), location!!.long.toDouble()))
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location!!.lat.toDouble(),
                                    location!!.long.toDouble()
                                ), MAX_ZOOM
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
                        markers[currentMarker!!.id] = currentSelectedCourier

                        //move mMap camera
                        moveCamera(latLng)
                    }

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

        DEFAULT_ZOOM = mMap.cameraPosition.zoom

        mMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    latLng,
                    DEFAULT_ZOOM
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


    private fun loadAllCouriersFromFB() {
        var iconFactory = IconGenerator(context!!)

        var marker: Marker? = null
        var markersList: ArrayList<Marker>? = ArrayList()
        var latLngList: ArrayList<LatLng>? = ArrayList()
        var currentLatLng: LatLng? = null
        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getAllCouriers { success, data ->
                if (success) {
                    couriersList = data!!
                    mMap.clear()
//                    mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(activity!!))

                    markersList?.clear()
                    markers.clear()

                    data!!.forEach {
                        if (!it.location.lat.isNullOrEmpty() && !it.location.long.isNullOrEmpty()) {
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
                                            iconFactory.makeIcon(it.name)


//                                            BitmapFactory.decodeResource(
//                                                resources,
//                                                R.mipmap.ic_launcher
//                                            )
                                        )
                                    )


                            )



                            if (markersList!!.size < data.size) {
                                marker?.tag = it
                                markersList!!.add(marker!!)
                                markers[marker!!.id] = it
                            }

                        }
                    }

                    if (markersList!!.size > 0)
                        prepareGetAllCourierView(markersList)

                } else {
                    Log.d(TAG, data.toString())
                }
            }


        } else
            Alert.showMessage(context!!, getString(R.string.no_internet))

    }

    private fun setCameraView() {

//        // Set a boundary to start
//        val bottomBoundary = mUserPosition.getGeo_point().getLatitude() - .1
//        val leftBoundary = mUserPosition.getGeo_point().getLongitude() - .1
//        val topBoundary = mUserPosition.getGeo_point().getLatitude() + .1
//        val rightBoundary = mUserPosition.getGeo_point().getLongitude() + .1
//
//        mMapBoundary = LatLngBounds(
//            LatLng(bottomBoundary, leftBoundary),
//            LatLng(topBoundary, rightBoundary)
//        )
//
//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
    }

    private fun prepareGetAllCourierView(
        markersList: ArrayList<Marker>
    ) {


        var builder = LatLngBounds.builder()
        markersList.forEach { marker ->
            builder.include(marker.position)
            var courier = marker?.tag as Courier
//            marker!!.title=courier.name
//            marker.snippet=courier.name
//            marker.showInfoWindow()

        }
        var bounds = builder.build()
        // begin new code:
        var width = resources.displayMetrics.widthPixels;
        var height = resources.displayMetrics.heightPixels;
        var padding = (width * 0.12).toInt()
//        var padding = 0 // offset from edges of the mMap in pixels
        var cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

//        mMap.setMaxZoomPreference(12.0F)
        if (!AppConstants.isMoving)
            mMap.animateCamera(cu)


    }

    override fun onDestroy() {
        super.onDestroy()
        AppConstants.isMoving = false
    }

    private fun getTAskDirection(task: Task) {
//        directionMode = false
        calculateDirections(task, false)

    }

    private fun getDirection(task: Task) {
        directionMode = false
        var firstStop = task.stopsmodel.first()
        var lastStop = task.stopsmodel.last()

        var pickUp = LatLng(
            firstStop.Latitude!!,
            firstStop.Longitude!!
        )
        var dropOff = LatLng(
            lastStop.Latitude!!,
            lastStop.Longitude!!
        )

        waypoints = ArrayList()
        task.stopsmodel.forEach {
            if (it.StopTypeID == 3)
                waypoints.add(
                    LatLng(
                        it.Latitude!!,
                        it.Longitude!!
                    )
                )
        }

        if (waypoints.size > 0) {
            GoogleDirection.withServerKey(context!!.getString(R.string.google_maps_key))
                .from(pickUp)
                .and(waypoints)
                .to(dropOff)
                .transportMode(TransportMode.DRIVING)
                .execute(object : DirectionCallback {
                    override fun onDirectionSuccess(direction: Direction?) {
                        if (direction!!.isOK) {
                            // Do something
                        } else {
                            // Do something
                        }
                    }

                    override fun onDirectionFailure(t: Throwable?) {

                    }


                });
        } else {

        }


    }

    private fun calculateDirections(
        task: Task,
        showAlternatives: Boolean
    ) {
        var firstStop = task.stopsmodel.first()
        var lastStop = task.stopsmodel.last()


        Log.d(
            TAG,
            "calculateDirections: calculating directions."
        )
        val destination = com.google.maps.model.LatLng(
            lastStop.Latitude!!,
            lastStop.Longitude!!
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(showAlternatives)

        directions.origin(
            com.google.maps.model.LatLng(
                firstStop.Latitude!!,
                firstStop.Longitude!!
            )
        )
        Log.d(TAG, "calculateDirections: destination: $destination")


        task.stopsmodel.forEach {

            if (it.StopTypeID == 3) {
                directions.waypoints(
                    com.google.maps.model.LatLng(
                        it.Latitude!!,
                        it.Longitude!!
                    )
                )

            }
            directions.optimizeWaypoints(true)
        }





        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult?> {
                override fun onResult(result: DirectionsResult?) {
                    result!!.routes[0].legs.forEach {
                        Log.d(
                            TAG,
                            "LEG: duration: " + it.duration
                        );
                        Log.d(
                            TAG,
                            "LEG: distance: " + it.distance
                        );
                        Log.d("LEG DATA", it.toString())
                    }

                    addPolylinesToMap(result!!)

                }

                override fun onFailure(e: Throwable) {
//                    Alert.hideProgress()
                    Log.e(
                        TAG,
                        "calculateDirections: Failed to get directions: " + e.message
                    )

                    Alert.showMessage(context!!, "Can't find a way there.")
                }
            })

    }

    private fun conevrtMetersToKilometers(meters: Long): Float {
        var kilometers = 0F
        kilometers = (meters * 0.001).toFloat()

        return kilometers
    }

    private fun addPolylinesToMap(result: DirectionsResult) {

        Handler(Looper.getMainLooper()).post {
            Log.d(
                TAG,
                "run: result routes: " + result.routes.size
            )
            if (mPolyLinesData.size > 0) {
                for (polylineData in mPolyLinesData) {
                    polylineData.polyline.remove()
                }
                mPolyLinesData.clear()
                mPolyLinesData = java.util.ArrayList<PolylineData>()
            }
            var duration = 999999999.0

            for (route in result.routes) {
                val decodedPath =
                    PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath: MutableList<LatLng> =
                    java.util.ArrayList()
                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) { //                        Log.d(TAG, "run: latlng: " + latLng.toString());
                    newDecodedPath.add(
                        LatLng(
                            latLng.lat,
                            latLng.lng
                        )
                    )
                }


                // highlight the fastest route and adjust camera
                val tempDuration =
                    route.legs[0].duration.inSeconds.toDouble()

                if (tempDuration < duration) {
                    mMap.clear()
                    mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    polyline =
                        mMap.addPolyline(PolylineOptions().addAll(newDecodedPath)) // add marker
                    polyline.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
                    polyline.isClickable = true
                    mPolyLinesData.add(PolylineData(polyline, route.legs[0]))

                    duration = tempDuration
                    val southwest = LatLng(route.bounds.southwest.lat,route.bounds.southwest.lng)
                    val northeast = LatLng(route.bounds.northeast.lat,route.bounds.northeast.lng)
                    setCameraWithCoordinationBoundsWithLatLng(southwest!!,northeast)
//                    zoomRoute(polyline.points)

                }

            }
            setTripStopsMarker(AppConstants.CurrentSelectedTask)

        }

    }

    fun zoomRoute(lstLatLngRoute: List<LatLng?>?) {
        if (map == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(
            latLngPoint
        )
        val routePadding = 50
        val latLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600,
            null
        )
    }

    private fun setTripDirectionData(polylineData: PolylineData) {
        tvExpectedTime.text = polylineData.leg.duration.toString()
        tvExpectedDistance.text =
            "( " + polylineData.leg.distance.toString() + "  )"
    }

    private fun setTripData(leg: Leg) {
        tvExpectedTime.text = leg.duration.value.toString()
        tvExpectedDistance.text =
            "( " + leg.distance.value.toString() + "  )"
    }

    //convert vector to bitmap
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

//    public fun animateMarker( destination:Location,  marker:Marker) {
//    if (marker != null) {
//        var startPosition = marker.getPosition();
//        var endPosition =  LatLng(destination.getLatitude(), destination.getLongitude());
//
//        var startRotation = marker.getRotation();
//
//        var latLngInterpolator =  LatLngInterpolator.LinearFixed();
//        var valueAnimator = ValueAnimator.ofFloat(0F, 1F);
//        valueAnimator.setDuration(1000); // duration 1 second
//        valueAnimator.setInterpolator( LinearInterpolator());
//        valueAnimator.addUpdateListener( ValueAnimator.AnimatorUpdateListener() {
//            @Override fun onAnimationUpdate( animation:ValueAnimator) {
//                try {
//                    var v = animation.getAnimatedFraction();
//                    var newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
//                    marker.setPosition(newPosition);
////                    marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
//                } catch ( ex:Exception) {
//                    // I don't care atm..
//                }
//            }
//
//
//        });
//
//        valueAnimator.start();
//    }
//}


//private  fun computeRotation( fraction:Float,  start:Float,  end:Float):Float {
//    var normalizeEnd = end - start; // rotate start to 0
//    var normalizedEndAbs = (normalizeEnd + 360) % 360;
//
//    var direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
//     var rotation=0;
//    if (direction > 0) {
//        rotation = normalizedEndAbs;
//    } else {
//        rotation = normalizedEndAbs - 360;
//    }
//
//    var result = fraction * rotation + start;
//    return (result + 360) % 360;
//}

//    private interface LatLngInterpolator {
//    LatLng interpolate(float fraction, LatLng a, LatLng b);
//
//    class LinearFixed implements LatLngInterpolator {
//        @Override
//        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
//            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
//            double lngDelta = b.longitude - a.longitude;
//            // Take the shortest path across the 180th meridian.
//            if (Math.abs(lngDelta) > 180) {
//                lngDelta -= Math.signum(lngDelta) * 360;
//            }
//            double lng = lngDelta * fraction + a.longitude;
//            return new LatLng(lat, lng);
//        }
//    }
//}

    private fun setCameraWithCoordinationBounds(route: Route) {
        val southwest = route.bound.southwestCoordination.coordination
        val northeast = route.bound.northeastCoordination.coordination
        val bounds = LatLngBounds(southwest, northeast)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
    private fun setCameraWithCoordinationBoundsWithLatLng(southwest: LatLng,northeast:LatLng) {
        val bounds = LatLngBounds(southwest, northeast)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun onDirectionDataSuccess(direction: Direction) {

        var leg = Leg()
        if (direction.isOK) {
            val route = direction.routeList[0]
            val legCount = route.legList.size
            for (index in 0 until legCount) {
                leg = route.legList[index]
                mMap?.addMarker(MarkerOptions().position(leg.startLocation.coordination))
                if (index == legCount - 1) {
                    mMap?.addMarker(MarkerOptions().position(leg.endLocation.coordination))
                }
                val stepList = leg.stepList
                val polylineOptionList = DirectionConverter.createTransitPolyline(
                    context!!,
                    stepList,
                    5,
                    Color.RED,
                    3,
                    Color.BLUE
                )
                for (polylineOption in polylineOptionList) {
                    polyline = mMap?.addPolyline(polylineOption)
                    polyline.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
                    polyline.isClickable = false
                    mNewPolyLinesData.add(NewPolylineData(polyline, leg))
                }
            }
            setCameraWithCoordinationBounds(route)
            setTripData(route.legList[0])
            rlBottom.visibility = View.VISIBLE
        } else {
            Alert.showMessage(context!!, direction.status)
        }

//        newAddPolyLinesToMap(direction)
    }


    private fun onDirectionSuccess(direction: Direction) {
        var task = AppConstants.CurrentSelectedTask


        if (direction.isOK) {
            val route = direction.routeList[0]
            val legCount = route.legList.size
            for (index in 0 until legCount) {

                val leg = route.legList[index]
                val stop =
                    task.stopsmodel.find { it.Latitude == leg.startLocation.coordination.latitude && it.Longitude == leg.startLocation.coordination.longitude }
                Log.d(TAG, stop?.StopName.toString())
                Log.d(TAG, leg.toString())
                print(leg)
                var marker = mMap?.addMarker(
                    MarkerOptions().position(leg.startLocation.coordination)
                        .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_placeholder))
//                        .title("HI")

                ).showInfoWindow()
//                mMap?.addMarker(MarkerOptions().position(leg.startLocation.coordination).snippet(stop?.StopName))

                if (index == legCount - 1) {
                    mMap?.addMarker(
                        MarkerOptions().position(leg.endLocation.coordination)
                            .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_placeholder))
//                            .title("HI")

                    ).showInfoWindow()
//                    mMap?.addMarker(MarkerOptions().position(leg.startLocation.coordination).snippet(stop?.StopName))
                }
                val stepList = leg.stepList
                val polylineOptionList = DirectionConverter.createTransitPolyline(
                    context!!,
                    stepList,
                    5,
                    Color.RED,
                    3,
                    Color.BLUE
                )
                for (polylineOption in polylineOptionList) {
                    mMap?.addPolyline(polylineOption)
                }
            }
            setCameraWithCoordinationBounds(route)
//            setTripData(route.legList[0])
//            rlBottom.visibility = View.VISIBLE

        } else {
            Alert.showMessage(context!!, direction.status)
        }
    }

    private fun newAddPolyLinesToMap(direction: Direction) {
        Handler(Looper.getMainLooper()).post {
            Log.d(
                TAG,
                "run: result routes: " + direction.routeList.size
            )
            if (mNewPolyLinesData.size > 0) {
                for (polylineData in mNewPolyLinesData) {
                    polylineData.getPolyline().remove()
                }
                mNewPolyLinesData.clear()
                mNewPolyLinesData = java.util.ArrayList<NewPolylineData>()
            }
            var duration = 999999999.0

            for (route in direction.routeList) {

                var leg = route.legList[0]
                Log.d(
                    TAG,
                    "run: leg: " + route.legList[0].toString()
                )
                val decodedPath =
                    PolylineEncoding.decode(route.overviewPolyline.toString())
                val newDecodedPath: MutableList<LatLng> =
                    java.util.ArrayList()
                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) { //                        Log.d(TAG, "run: latlng: " + latLng.toString());
                    newDecodedPath.add(
                        LatLng(
                            latLng.lat,
                            latLng.lng
                        )
                    )
                }
                polyline =
                    mMap.addPolyline(PolylineOptions().addAll(newDecodedPath)) // add marker
                polyline.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
                polyline.isClickable = true

                mNewPolyLinesData.add(NewPolylineData(polyline, leg))
                // highlight the fastest route and adjust camera

                val tempDuration =
                    leg.duration.value.toDouble()
                if (tempDuration < duration) {
                    duration = tempDuration
                    onPolylineClick(polyline)
                    zoomRoute(polyline.points)
                    setTripData(leg)
                    rlBottom.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun requestDirection(task: Task) {

        directionMode = false
        firstStop = task.stopsmodel.first()
        lastStop = task.stopsmodel.last()

        var pickUp = LatLng(
            firstStop.Latitude!!,
            firstStop.Longitude!!
        )
        var dropOff = LatLng(
            lastStop.Latitude!!,
            lastStop.Longitude!!
        )

        waypoints = ArrayList()
        task.stopsmodel.forEach {
            if (it.StopTypeID == 3)
                waypoints.add(
                    LatLng(
                        it.Latitude!!,
                        it.Longitude!!
                    )
                )
        }

        if (waypoints.size > 0) {
            GoogleDirectionConfiguration.getInstance().isLogEnabled = BuildConfig.DEBUG
            GoogleDirection.withServerKey(context!!.getString(R.string.google_maps_key))
                .from(pickUp)
                .and(waypoints)
                .to(dropOff)
                .transportMode(TransportMode.DRIVING)
                .execute(
                    onDirectionSuccess = { direction -> onDirectionSuccess(direction) },
                    onDirectionFailure = { t -> onDirectionFailure(t) }
                )
        } else {
            GoogleDirectionConfiguration.getInstance().isLogEnabled = BuildConfig.DEBUG
            GoogleDirection.withServerKey(context!!.getString(R.string.google_maps_key))
                .from(pickUp)
                .to(dropOff)
                .transportMode(TransportMode.DRIVING)
                .execute(
                    onDirectionSuccess = { direction -> onDirectionSuccess(direction) },
                    onDirectionFailure = { t -> onDirectionFailure(t) }
                );
        }


    }

    private fun onDirectionFailure(t: Throwable) {
        Alert.showMessage(context!!, "Can't find a way there.")
    }

    private fun getUserCountry(context: Context): String? {
        try {
            var tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US)
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                var networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return null
    }

    private fun setTripStopsMarker(task: Task) {
        var speciaMarker: Marker? = null
        task.stopsmodel.forEach {
            when (it.StopTypeID) {
                1 -> {
                    val marker: Marker = mMap.addMarker(
                        MarkerOptions()
                            .icon(
                                bitmapDescriptorFromVector(
                                    context!!,
                                    R.drawable.ic_location_start
                                )
                            )
                            .position(LatLng(it.Latitude!!, it.Longitude!!))
                            .title(it.StopName)


                    )
                    mTripMarkers.add(marker)
                    marker.tag = it
                    marker.showInfoWindow()
                    Log.d("MARKER DATA", it.StopName)
//                    marker.showInfoWindow()
                }
                2 -> {
                    var tripData = TripData()
                    var snippetData =
                        getString(R.string.distance) + " " + tripData.distance.toString() + " - " + (getString(
                            R.string.duration
                        ) + " " + tripData.toString())

                   val  speciaMarker = mMap.addMarker(
                        MarkerOptions()
                            .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_location))
                            .position(LatLng(it.Latitude!!, it.Longitude!!))
                            .title(it.StopName)
                    )
                    mTripMarkers.add(speciaMarker!!)
                    speciaMarker?.tag = it
                    Log.d("MARKER DATA", it.StopName)
                    speciaMarker?.showInfoWindow()
                }
                3 -> {
//                    val marker: Marker = map.addMarker(
//                        MarkerOptions()
//                            .icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_stops))
//                            .position(LatLng(it.Latitude!!, it.Longitude!!))
//                            .title(it.StopName)
//                    )
//                    mTripMarkers.add(marker)
//                    marker.tag = it
//                    Log.d("MARKER DATA",it.StopName)
//                    marker.showInfoWindow()

                    val marker: Marker = mMap.addMarker(
                        MarkerOptions()
                            .icon(
                                bitmapDescriptorFromVector(
                                    context!!,
                                    R.drawable.ic_location_stop
                                )
                            )
                            .position(LatLng(it.Latitude!!, it.Longitude!!))
                            .title(it.StopName)
                    )
                    marker.showInfoWindow()
                    mTripMarkers.add(marker)
                    marker.tag = it
                    Log.d("MARKER DATA", it.StopName)
                    Log.d("Latitude", it.Latitude.toString())
                    Log.d("Longitude", it.Longitude.toString())


//                    marker.showInfoWindow()
                }
            }
        }

//        speciaMarker!!.showInfoWindow()
    }


    //endregion
}
