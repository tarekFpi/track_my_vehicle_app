package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.mainThread
import com.singularity.trackmyvehicle.utils.bitmapSizeByScall
import com.singularity.trackmyvehicle.utils.getTimeDuration
import com.singularity.trackmyvehicle.view.map.DrawMarker
import com.singularity.trackmyvehicle.view.map.DrawRouteMaps
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_direction.*
import java.util.*
import javax.inject.Inject


class DirectionActivity : AppCompatActivity(), OnMapReadyCallback {

    val TAG = "TAG"

    private val LOCATION_REFRESH_INTERVAL: Long = 5000


    private var mMap: GoogleMap? = null

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    private var mSelectedPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction)
        setSupportActionBar(toolbar)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key));
        }


        fetchCurrentLocation()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.placeAutocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: " + place.name)
                mSelectedPlace = place
                plotRoute()

            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

        autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
                ?.setOnClickListener { view ->
                    autocompleteFragment.setText("")
                    containerDetails.visibility = View.GONE
                    mMap?.clear()
                    addCurrentMarker()
                }

        fabCurrentLocation.setOnClickListener {
            focusOnCurrentStatus()
        }

        shimmerView.startShimmerAnimation()
    }

    private val REQUEST_LOCATION_PERMISSION: Int = 12345


    private var mLastLocation: LatLng? = null

    private fun plotRoute() {
        val destination = mSelectedPlace?.latLng
        if (destination != null) {
            drawRoute(destination)
        }
    }


    private fun drawRoute(destination: LatLng?) {
        if (mLastLocation == null) {
            Toasty.error(this, "Vehicle location is not available yet").show()
            return
        }
        mMap?.clear()
        addCurrentMarker()
        DrawRouteMaps.getInstance(this)
                .draw(mLastLocation, destination, mMap) { distance, duration ->
                    Log.d("TAG", "distance -> $distance, durationg -> $duration")
                    mainThread {
                        containerDetails.visibility = View.VISIBLE
                        txtDestination.text = "${mSelectedPlace?.name}"
                        mVehicleViewModel.getCurrentVehicle { vehicle ->
                            txtVehicle.text = vehicle?.vrn ?: "N/A"
                        }
                        txtDistance.text = "${(distance / 1000)} Km"
                        txtDuration.text = "${getTimeDuration(duration)} "
                    }
                }
        DrawMarker.getInstance(this).draw(mMap, mLastLocation, R.drawable.ic_location, "Origin Location")
        DrawMarker.getInstance(this).draw(mMap, destination, R.drawable.ic_location, "Destination Location")

        val bounds = LatLngBounds.Builder()
                .include(mLastLocation)
                .include(destination).build()
        val displaySize = Point()
        windowManager.defaultDisplay.getSize(displaySize)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x * 80 / 100, displaySize.y * 60 / 100, 30))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.isTrafficEnabled = true
        configureMapForFirst()
        addMyLocationButton()

    }

    private fun addMyLocationButton() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
            return
        }
        mMap?.isMyLocationEnabled = true


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        val locationButton = (mapFragment.view?.findViewById<View>(Integer.parseInt("1"))
                ?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
        }
        rlp.setMargins(0, 0, 30, 30)

    }

    private fun configureMapForFirst() {
        val displayMetrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels


        val BANGLADESH = LatLngBounds(LatLng(20.86382, 88.15638), LatLng(26.33338, 92.30153))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(BANGLADESH, width, height, 0))
    }

    companion object {
        fun intent(context: Context): Unit {
            val intent = Intent(context, DirectionActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    addCurrentMarker()
                    plotRoute()
                } else {
                    Toasty.error(this, "Permission denied").show()
                }
                return
            }

        }
    }

    private var mCurrentVehicleStatus: VehicleStatus? = null
    private var mCurrentVehicleStatusLiveData: LiveData<Resource<VehicleStatus>>? = null
    private val mCurrentVehicleStatusObserver = Observer<Resource<VehicleStatus>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            com.singularity.trackmyvehicle.network.Status.SUCCESS -> {
                shimmerView.stopShimmerAnimation()
                updateCurrentVehicleStatus(data.data)
            }
            com.singularity.trackmyvehicle.network.Status.LOADING -> {
                shimmerView.startShimmerAnimation()
            }
            com.singularity.trackmyvehicle.network.Status.ERROR -> {
                shimmerView.stopShimmerAnimation()
            }
        }
    }

    private val mCurrentStatusHandler = Handler()

    private val mCurrentStatusRunnable = object : Runnable {
        override fun run() {
            fetchCurrentLocation()
            mCurrentStatusHandler.postDelayed(this, LOCATION_REFRESH_INTERVAL)
        }
    }


    private var mMarker: Marker? = null

    private fun updateCurrentVehicleStatus(vehicleStatus: VehicleStatus?) {
        mCurrentVehicleStatus = vehicleStatus
        mLastLocation = LatLng(vehicleStatus?.location?.latitude?.toDoubleOrNull()
                ?: 0.0, vehicleStatus?.location?.longitude?.toDoubleOrNull() ?: 0.0)

        addCurrentMarker()
    }

    private fun addCurrentMarker() {
        val icon = getBitmapDescriptor()

        mLastLocation?.let {
            mMarker?.remove()
            val markerOption = MarkerOptions().position(it)
                    .title(mCurrentVehicleStatus?.location?.place)
                    .icon(icon)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .rotation(mCurrentVehicleStatus?.location?.direction?.toFloatOrNull() ?: 0.0f)

            mMarker = mMap?.addMarker(markerOption)
        }
    }

    fun fetchCurrentLocation() {
        mCurrentVehicleStatusLiveData?.removeObserver(mCurrentVehicleStatusObserver)

        mCurrentVehicleStatusLiveData = mVehicleViewModel
                .getCurrentVehicleStatus()

        mCurrentVehicleStatusLiveData?.observe(this, mCurrentVehicleStatusObserver)
    }

    override fun onPause() {
        super.onPause()
        mCurrentStatusHandler.removeCallbacks(mCurrentStatusRunnable)
    }

    override fun onResume() {
        super.onResume()
        mCurrentStatusHandler.post(mCurrentStatusRunnable)

    }

    private fun getBitmapDescriptor(): BitmapDescriptor? {
        var carIcon = BitmapFactory.decodeResource(this.resources, R.drawable.ic_car_icon)
        carIcon = bitmapSizeByScall(carIcon, 0.5f)
        val icon = BitmapDescriptorFactory.fromBitmap(carIcon)
        return icon
    }

    fun focusOnCurrentStatus() {
        if (mMap == null || mCurrentVehicleStatus == null)
            return
        val latLng = LatLng(mCurrentVehicleStatus?.location?.latitude?.toDoubleOrNull()
                ?: 0.toDouble(), mCurrentVehicleStatus?.location?.longitude?.toDoubleOrNull()
                ?: 0.toDouble())
        mMarker?.remove()
        val icon = getBitmapDescriptor()
        val markerOption = MarkerOptions().position(latLng)
                .title(mCurrentVehicleStatus?.location?.place)
                .icon(icon)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(mCurrentVehicleStatus?.location?.direction?.toFloatOrNull() ?: 0.0f)
        mMarker = mMap?.addMarker(markerOption)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

}
