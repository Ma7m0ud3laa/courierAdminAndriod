package com.kadabra.agent.courier


import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
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
import android.view.animation.LinearInterpolator
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.google.android.material.snackbar.Snackbar
import com.kadabra.Networking.NetworkManager
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.direction.TaskLoadedCallback
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.firebase.LatLngInterpolator
import com.kadabra.agent.model.Courier
import com.kadabra.agent.model.Stop
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.fragment_courier.*
import java.lang.Exception
import java.util.*


class CourierFragment : BaseFragment(), IBottomSheetCallback, OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, TaskLoadedCallback, View.OnClickListener {


    //region Members
    private lateinit var currentView: View
    private lateinit var mMap: GoogleMap
    private lateinit var ivBack: ImageView
    private lateinit var ivSearchMarker: ImageView
    private lateinit var btnConfirmLocation: Button
    private  var currentSelectedCourier= Courier()

    private var firstTimeFlag = true
    private var listener: IBottomSheetCallback? = null
    var searchMode = false
    private var isFirstTime = true
    private var currentMarker: Marker? = null
    private var stop: Stop = Stop()
    var address = ""
    var city = ""
    var geoCoder: Geocoder? = null
    var addresList: List<Address>? = null
    private lateinit var selectedLatLng: LatLng
    var markers = WeakHashMap <String, Courier>()
    var currentSelectedMarker: Marker? = null
    private val TAG = CourierFragment::class.java!!.simpleName

    private lateinit var polylines: List<Polyline>
    private var currentPolyline: Polyline? = null
    private var placesClient: PlacesClient? = null
    //    private var materialSearchBar: MaterialSearchBar? = null
    private var predictionList: List<AutocompletePrediction>? = null
    private var DEFAULT_ZOOM = 15f
    private var mapView: View? = null
    //endregion
    private var currentSelectedCourierId = 0


    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppConstants.isMoving=false //required for reget all the couriers data from firebase
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
//        materialSearchBar = currentView.findViewById(R.id.searchBar)
        ivBack.setOnClickListener(this)
        btnConfirmLocation.setOnClickListener(this)

        //////////////////////////////////////////////////////////////
        var mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapView = mapFragment!!.view


        Places.initialize(context!!, context!!.getString(R.string.google_maps_key))
        placesClient = Places.createClient(context!!)
        val token = AutocompleteSessionToken.newInstance()


//        materialSearchBar!!.setOnSearchActionListener(object :
//            MaterialSearchBar.OnSearchActionListener {
//            override fun onSearchStateChanged(enabled: Boolean) {
//
//            }
//
//            override fun onSearchConfirmed(text: CharSequence) {
//                activity!!.startSearch(text.toString(), true, null, true)
//            }
//
//            override fun onButtonClicked(buttonCode: Int) {
//                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
//                    //opening or closing a navigation drawer
//                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
//                    materialSearchBar!!.disableSearch()
//                }
//            }
//        })
//
//        materialSearchBar!!.addTextChangeListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                val predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                    .setTypeFilter(TypeFilter.ADDRESS)
//                    .setSessionToken(token!!)
//                    .setQuery(s.toString())
//                    .build()
//                placesClient!!.findAutocompletePredictions(predictionsRequest)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val predictionsResponse = task.result
//                            if (predictionsResponse != null) {
//                                predictionList = predictionsResponse.autocompletePredictions
//                                val suggestionsList = ArrayList<String>()
//                                for (i in predictionList!!.indices) {
//                                    val prediction = predictionList!!.get(i)
//                                    suggestionsList.add(prediction.getFullText(null).toString())
//                                }
//                                materialSearchBar!!.updateLastSuggestions(suggestionsList)
//                                if (!materialSearchBar!!.isSuggestionsVisible) {
//                                    materialSearchBar!!.showSuggestionsList()
//                                }
//                            }
//                        } else {
//                            Log.i("mytag", "prediction fetching task unsuccessful")
//                        }
//                    }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })
//
//
//        materialSearchBar!!.setSuggstionsClickListener(object :
//            SuggestionsAdapter.OnItemViewClickListener {
//            override fun OnItemClickListener(position: Int, v: View) {
//                if (position >= predictionList!!.size) {
//                    return
//                }
//                val selectedPrediction = predictionList!![position]
//                val suggestion = materialSearchBar!!.lastSuggestions[position].toString()
//                materialSearchBar!!.text = suggestion
//
//                Handler().postDelayed({ materialSearchBar!!.clearSuggestions() }, 1000)
//                val imm = context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                imm?.hideSoftInputFromWindow(
//                    materialSearchBar!!.windowToken,
//                    InputMethodManager.HIDE_IMPLICIT_ONLY
//                )
//                val placeId = selectedPrediction.placeId
//                val placeFields = listOf(Place.Field.LAT_LNG)
//
//                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
//                placesClient!!.fetchPlace(fetchPlaceRequest)
//                    .addOnSuccessListener { fetchPlaceResponse ->
//                        val place = fetchPlaceResponse.place
//                        if (place.name != null)
//                            Log.i("mytag", "Place found: " + place.name)
//                        else
//                            Log.i("mytag", "Place found: " + "No Name is found")
//
//                        val latLngOfPlace = place.latLng
//                        if (latLngOfPlace != null) {
//                            ivSearchMarker.visibility = View.VISIBLE
//                            mMap.moveCamera(
//                                CameraUpdateFactory.newLatLngZoom(
//                                    latLngOfPlace,
//                                    DEFAULT_ZOOM
//                                )
//                            )
//                        }
//                    }.addOnFailureListener { e ->
//                        if (e is ApiException) {
//                            e.printStackTrace()
//                            val statusCode = e.statusCode
//                            Log.i("mytag", "place not found: " + e.message)
//                            Log.i("mytag", "status code: $statusCode")
//                        }
//                    }
//            }
//
//            override fun OnItemDeleteListener(position: Int, v: View) {
//
//            }
//        })

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
                else
                    listener?.onBottomSheetSelectedItem(2)
            }
            R.id.btnConfirmLocation -> {
                getFullLocationData(selectedLatLng.latitude, selectedLatLng.longitude)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.setOnMarkerClickListener(this)


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

//            selectedLatLng = mMap.cameraPosition.target

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



        mMap.setOnMyLocationButtonClickListener {
            //            if (materialSearchBar!!.isSuggestionsVisible)
//                materialSearchBar!!.clearSuggestions()
//            if (materialSearchBar!!.isSearchEnabled)
//                materialSearchBar!!.disableSearch()
            false
        }

    }

    override fun onMarkerClick(courierMarker: Marker?): Boolean {


        if (!searchMode &&DEFAULT_ZOOM<20) {

            var currentCourier = courierMarker?.tag as Courier
            if (currentCourier != null && currentCourier.CourierId!! > 0) {
                AppConstants.isMoving = true
                if (currentSelectedCourierId != currentCourier.CourierId) {
                    firstTimeFlag = true
                    getCurrentCourierLocation(currentCourier.CourierId!!)
                }
            }

        }


        return false
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
                                ), 20F
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

//                        currentMarker!!.position = latLng
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
        var marker: Marker? = null
        var markersList: ArrayList<Marker>? = ArrayList()
        var latLngList: ArrayList<LatLng>? = ArrayList()
        var currentLatLng: LatLng? = null
        if (NetworkManager().isNetworkAvailable(context!!)) {
            FirebaseManager.getAllCouriers { success, data ->
                if (success) {

                    mMap.clear()
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
                                            BitmapFactory.decodeResource(
                                                resources,
                                                R.mipmap.ic_launcher
                                            )
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
    //endregion
}
