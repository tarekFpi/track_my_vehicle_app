package com.singularity.trackmyvehicle.view.map

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.maps.android.MarkerManager
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.repository.interfaces.OnMapClicked
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.utils.cluster.MyClusterItem
import com.singularity.trackmyvehicle.utils.polylineDecoder.Point
import com.singularity.trackmyvehicle.utils.polylineDecoder.PolylineDecoder
import com.singularity.trackmyvehicle.view.activity.MonitoringActivity
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil


class MapDrawer(
        private val mMapFragment: SupportMapFragment,
        private val mVehicleRepository: VehicleRepository,
        private val mPrefRepository: PrefRepository,
        private val onMapClicked: OnMapClicked? = null
) : OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<MyClusterItem>,
        ClusterManager.OnClusterItemClickListener<MyClusterItem>,
        GoogleMap.OnMapClickListener {

    val POLY_LINE_WIDTH: Float
        get() = 8.0f
//        get() = FirebaseRemoteConfig.getInstance().getString("polyline_width")?.toFloatOrNull()
                ?: 5.0f


    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    var mMap: GoogleMap? = null

    private var mCurrentVehicleStatus: VehicleStatus? = null

    private var mCurrentDate: DateTime? = null

    private var mPolyLine: Polyline? = null
    private var mLastRoute: List<VehicleRoute> = ArrayList()

    private var mLastVehicleRoute: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData> = ArrayList()

    private var mMarker: Marker? = null
    private var mRouteStart: Marker? = null
    private var mRouteEnd: Marker? = null
    private var mLatLngByTime: HashMap<Int, LatLng> = HashMap()

    private var mRoutesByTime: HashMap<Int, VehicleRoute> = HashMap()

    private var mVehicleRoutesByTime: HashMap<Int, VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData> = HashMap()
    private var mCurrentVehicleLocationData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteGeoLocationPosition> = ArrayList()

    val polylineDecoder = PolylineDecoder()

    var parkingMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var parkingMarker: ArrayList<Marker> = ArrayList()

    var suddenAccelerationMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var suddenAccelerationMarker: ArrayList<Marker> = ArrayList()

    var harshBreakMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var harshBreakMarker: ArrayList<Marker> = ArrayList()

    var speedViolationMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var speedViolationMarker: ArrayList<Marker> = ArrayList()

    var engineOnOffMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var engineOnOffMarker: ArrayList<Marker> = ArrayList()

    var powerDownMarkerOptions: ArrayList<MarkerOptions> = ArrayList()
    var powerDownMarker: ArrayList<Marker> = ArrayList()

    var idleFlagMarkerOptionList: ArrayList<MarkerOptions> = ArrayList()
    var idleFlagMarkerList: ArrayList<Marker> = ArrayList()

    var motionStateVehicleList: ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState> = ArrayList()

    var polyLineColorList: ArrayList<PolylineOptions> = ArrayList()

    var acumulatedDistanceInMeter = 0
    var acumulatedDurationInSecond = 0

    var mClusterManager: ClusterManager<MyClusterItem>? = null
    var allVehiclesLatLngDataList: ArrayList<MyClusterItem> = ArrayList()

    var allVehiclesDataList: ArrayList<Terminal> = ArrayList()

    var isMapSelectedType = true

    var isVirtualWatchman = false
    var selectedLatitude = 0.00
    var selectedLongitude = 0.00
    var radiusMeter = 0.00

    var isClusterOnClicked: Boolean? = false

    fun setLocationList(list: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteGeoLocationPosition>) {
        mCurrentVehicleLocationData = list
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
        mMap?.setInfoWindowAdapter(AppInfoWindowAdapter(mMapFragment.requireContext()))
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.isTrafficEnabled = true
        mMap?.uiSettings?.isCompassEnabled = false
        
        val displayMetrics = DisplayMetrics()
        mMapFragment.activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val BANGLADESH = LatLngBounds(LatLng(20.86382, 88.15638), LatLng(26.33338, 92.30153))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(BANGLADESH, width, height, 0))

//        setMapType(isMapSelectedType)

        mMap?.setOnInfoWindowClickListener {
           it.showInfoWindow()
        }

        val markerManager = MarkerManager(mMap)

        mClusterManager = ClusterManager(mMapFragment.context, mMap, markerManager)
        mClusterManager?.renderer = ClusterRenderer()
        mClusterManager?.setOnClusterClickListener(this)
        mClusterManager?.setOnClusterItemClickListener(this)

        mMap?.setOnCameraIdleListener(mClusterManager)
//        mMap?.isMyLocationEnabled = true
        mMap?.setOnMarkerClickListener(mClusterManager)
        if (mLastRoute.isNotEmpty()) {
            drawRoute(mLastRoute, mCurrentDate)
        }

        if (mLastVehicleRoute.isNotEmpty()) {
            drawVehicleRoute(mLastVehicleRoute, mCurrentDate)
        }

        if (mCurrentVehicleStatus != null) {
           placeMarkerAtCurrentPosition(mCurrentVehicleStatus, isFirstTime = true)
           focusOnCurrentVehicle(mCurrentVehicleStatus!!)
        }
        /*if (mCurrentVehicleStatus != null) {
            placeMarkerAtCurrentPositionFromVehicleMonitoring(mCurrentVehicleStatus)
        }*/

        val polylineOptions = PolylineOptions()
                .visible(true)
                .zIndex(1f)
        polylineOptions.add()

        addSelectedRadiusCircle(isVirtualWatchman, LatLng(selectedLatitude, selectedLongitude), radiusMeter)

//        addClusterItems()

        mMap?.setOnMapClickListener(this)
    }


    inner class ClusterRenderer : DefaultClusterRenderer<MyClusterItem>(mMapFragment.context, mMap, mClusterManager) {

        init {
            minClusterSize = 1

        }

        override fun onBeforeClusterRendered(cluster: Cluster<MyClusterItem>, markerOptions: MarkerOptions) {
            markerOptions.icon(getClusterIcon(cluster))
        }

        override fun onBeforeClusterItemRendered(item: MyClusterItem,
                                                 markerOptions: MarkerOptions) {
            markerOptions.title("").icon(smallMarkerForAllVehicles(item.index)).anchor(0.5f, 0.5f)

        }

        /*override fun onClusterRendered(cluster: Cluster<MyClusterItem>?, marker: Marker?) {

            getMarker(cluster).showInfoWindow()
        }*/

        private val clusterIconGenerator = IconGenerator(mMapFragment.context)

        fun getClusterIcon(cluster: Cluster<MyClusterItem>): BitmapDescriptor {
            clusterIconGenerator.setBackground(mMapFragment.context?.let { ContextCompat.getDrawable(it, R.drawable.ic_cluster) })
            val myInflater: LayoutInflater = mMapFragment.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val clusterView: View = myInflater.inflate(R.layout.cluster_view, null, false)
            clusterIconGenerator.setContentView(clusterView)
            val icon: Bitmap = clusterIconGenerator.makeIcon(cluster.size.toString())
            return BitmapDescriptorFactory.fromBitmap(icon)
        }
    }

    fun setVehicleList(vehicles: ArrayList<Terminal>, isAddCluster: Boolean) {
        allVehiclesDataList = vehicles

        if (isAddCluster) addClusterItems()
    }

    fun focusOnBDLatLng() {
        val displayMetrics = DisplayMetrics()
        mMapFragment.activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val BANGLADESH = LatLngBounds(LatLng(20.86382, 88.15638), LatLng(26.33338, 92.30153))
//        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(BANGLADESH, width, height, 0))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(BANGLADESH, 14))
    }

    fun setMapType(isMapType: Boolean) {
        isMapSelectedType = isMapType

        if (isMapSelectedType) {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        } else {
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

    }

    fun setNightMode(isNightMode: Boolean) {
        val style = MapStyleOptions.loadRawResourceStyle(mMapFragment.requireContext(), R.raw.mapstyle_night)

        if (isNightMode) {
            mMap?.setMapStyle(style)
        } else {
            mMap?.setMapStyle(null)
        }

    }

    fun setMapMode(isMapDefault: Boolean, isMapModeLight: Boolean) {
        val style = MapStyleOptions.loadRawResourceStyle(mMapFragment.requireContext(), R.raw.mapstyle_night)

        mMap?.mapType = if (isMapDefault) GoogleMap.MAP_TYPE_NORMAL else GoogleMap.MAP_TYPE_SATELLITE
        mMap?.setMapStyle(if (isMapModeLight) null else style)
    }

    fun drawRoute(routes: List<VehicleRoute>?, currentDate: DateTime?) {
        if (routes == null || routes.isEmpty())
            return

        mLastRoute = routes
        mCurrentDate = currentDate

        if (mMap == null) {
            return
        }

        BackGroundTasker<PolylineOptions>(object : BackGroundTasker.Helper<PolylineOptions> {
            override fun onBackground(): PolylineOptions {

//             val mapColor = Color.parseColor(POLYLINE_COLOR)
                val mapColor = Color.parseColor(
                        FirebaseRemoteConfig.getInstance().getString("polyline_color"))
                val options = PolylineOptions().width(POLY_LINE_WIDTH).color(mapColor)
                        .geodesic(true)
                        .visible(true)
                        .zIndex(1f)

                mLastRoute.forEach { vehicleRoute ->
                    val date = DateTime.parse(vehicleRoute.updatedAt,
                            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                    val minuteOfDay = date.minuteOfDay().get()
                    val lat = vehicleRoute.location.latitude?.toDoubleOrNull()
                    val lon = vehicleRoute.location.longitude?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        mLatLngByTime[minuteOfDay] = LatLng(lat, lon)
                    }

                    mRoutesByTime[minuteOfDay] = vehicleRoute

                    if (!((mRoutesByTime[minuteOfDay - 1]?.engineStatus?.toUpperCase() == "ON" && mRoutesByTime[minuteOfDay]?.engineStatus?.toUpperCase() == "OFF")
                                    || mRoutesByTime[minuteOfDay]?.engineStatus?.toUpperCase() == "ON")) {
                        if (mRoutesByTime[minuteOfDay - 1] != null) {
                            mRoutesByTime[minuteOfDay] = mRoutesByTime[minuteOfDay - 1]!!
                        }
                        if (mLatLngByTime[minuteOfDay - 1] != null) {
                            mLatLngByTime[minuteOfDay] = mLatLngByTime[minuteOfDay - 1]!!

                        }
                    }


                    // UnComment this to plot original points
                    /*if (((mRoutesByTime[minuteOfDay - 1]?.engineStatus?.toUpperCase() == "ON" && mRoutesByTime[minuteOfDay]?.engineStatus?.toUpperCase() == "OFF")
                                    || mRoutesByTime[minuteOfDay]?.engineStatus?.toUpperCase() == "ON")) {
                        val point = LatLng(vehicleRoute.location.latitude.toDoubleOrNull(), vehicleRoute.location.longitude.toDoubleOrNull())
                        options.add(point)
                    }*/
                }

                val veicleRoutePolyLine = mVehicleRepository.getVeicleRoutePolyLine(
                        mPrefRepository.currentVehicle(), currentDate
                        ?: DateTime.now())
                val encodedPolyLine = veicleRoutePolyLine?.polyline ?: ""
                if (!encodedPolyLine.isEmpty()) {
                    val points = polylineDecoder.decode(encodedPolyLine)
                    points.forEach { point: Point? ->
                        point?.let { it ->
                            options.add(
                                    LatLng(it.lat, it.lng)
                            ).zIndex(1f)
                        }
                    }
                } else if (veicleRoutePolyLine?.latlagns?.size != 0) {
                    val points = veicleRoutePolyLine?.latlagns
                    points?.forEach { point: Point? ->
                        point?.let { it ->
                            options.add(
                                    LatLng(it.lat, it.lng)
                            ).zIndex(1f)
                        }
                    }
                }

                return options
            }

            override fun onForegound(result: PolylineOptions?) {
                mPolyLine?.remove()
                mRouteStart?.remove()
                mRouteEnd?.remove()

                val firstPosition = mLastRoute.firstOrNull {
                    it.location?.latitude?.toDoubleOrNull() != null &&
                            it.location?.longitude?.toDoubleOrNull() != null
                }

                firstPosition?.let {
                    val startMarkerOption = MarkerOptions()
                            .position(LatLng(it.location?.latitude?.toDoubleOrNull() ?: 0.0,
                                    it.location?.longitude?.toDoubleOrNull() ?: 0.0))
                            .title("Start")
                            .icon(getStartFlag())
                            .flat(false)
                            .infoWindowAnchor(0.5F, 0.5F)
                            .anchor(0.5f, 0.5f)
                            .rotation(0F)
                    mRouteStart = mMap?.addMarker(startMarkerOption)
                }

                val endPosition = mLastRoute.lastOrNull {
                    it.location?.latitude?.toDoubleOrNull() != null &&
                            it.location?.longitude?.toDoubleOrNull() != null
                }

                endPosition?.let {
                    val endMarkerOption = MarkerOptions()
                            .position(LatLng(it.location?.latitude?.toDoubleOrNull() ?: 0.0,
                                    it.location?.longitude?.toDoubleOrNull() ?: 0.0))
                            .title("End")
                            .icon(getEndFlag())
                            .flat(false)
                            .infoWindowAnchor(0.5F, 0.5F)
                            .anchor(0.5f, 0.5f)
                            .rotation(0F)
                    mRouteEnd = mMap?.addMarker(endMarkerOption)
                }


                mPolyLine = mMap?.addPolyline(result?.zIndex(1f))
                val lastRoute = try {
                    mLastRoute.lastOrNull {
                        it.location?.latitude?.toDoubleOrNull() != null &&
                                it.location?.longitude?.toDoubleOrNull() != null
                    }
                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    null
                }
                val lat = lastRoute?.location?.latitude?.toDoubleOrNull()
                val lon = lastRoute?.location?.longitude?.toDoubleOrNull()

                val builder = LatLngBounds.builder()
                result?.points?.forEach { builder.include(it) }

                if (lastRoute != null && lat != null && lon != null) {
                    if ((result?.points?.size ?: 0) != 0)
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                }
            }
        })

    }

    fun drawParkingRoute(parkings: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        placeParkingMarkerOnMap(parkings, isChecked)
    }

    fun drawSuddenAccelerationRoute(suddenAcceleration: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        suddenAccelerationMarkerOnMap(suddenAcceleration, isChecked)
    }

    fun drawHarshBreakRoute(harshBreak: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        harshBreakMarkerOnMap(harshBreak, isChecked)
    }

    fun drawSpeedViolationRoute(speedViolation: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        speedViolationMarkerOnMap(speedViolation, isChecked)
    }

    fun drawEngineOnOffRoute(engineOnOff: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        placeEngineOnOffMarkerOnMap(engineOnOff, isChecked)
    }

    fun drawPowerDownRoute(engineOnOff: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, currentDate: DateTime?, isChecked: Boolean) {
        placePowerDownMarkerOnMap(engineOnOff, isChecked)
    }

    fun drawVehicleRoute(routes: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>?, currentDate: DateTime?) {
        mMarker = null
        mMap?.clear()

        if (routes == null || routes.isEmpty())
            return

        mLastVehicleRoute = routes
        mCurrentDate = currentDate

        if (mMap == null) {
            return
        }

        BackGroundTasker<PolylineOptions>(object : BackGroundTasker.Helper<PolylineOptions> {

            override fun onBackground(): PolylineOptions {
                val options = PolylineOptions().width(POLY_LINE_WIDTH)
                        .geodesic(true)
                        .visible(true)
                        .zIndex(1f)

                mLastVehicleRoute.forEach { vehicleRoute ->
                    mMapFragment.activity?.runOnUiThread {
                        addCircle(LatLng(vehicleRoute.terminalDataLatitude?.toDouble()
                                ?: 0.0, vehicleRoute.terminalDataLongitude?.toDouble() ?: 0.0))
                    }

                    acumulatedDistanceInMeter += (vehicleRoute.distanceMeter?.toDouble()?.toInt()
                            ?: 0)
                    acumulatedDurationInSecond += (vehicleRoute.durationSecond?.toDouble()?.toInt()
                            ?: 0)

                    val date = DateTime.parse(vehicleRoute.terminalDataTime,
                            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                    val minuteOfDay = date.minuteOfDay().get()
                    val lat = vehicleRoute.terminalDataLatitude?.toDoubleOrNull()
                    val lon = vehicleRoute.terminalDataLongitude?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        mLatLngByTime[minuteOfDay] = LatLng(lat, lon)
                    }

                    mVehicleRoutesByTime[minuteOfDay] = vehicleRoute

                    if (mVehicleRoutesByTime[minuteOfDay - 1] != null) {
                        mVehicleRoutesByTime[minuteOfDay] = mVehicleRoutesByTime[minuteOfDay - 1]!!
                    }
                    if (mVehicleRoutesByTime[minuteOfDay - 1] != null) {
                        mVehicleRoutesByTime[minuteOfDay] = mVehicleRoutesByTime[minuteOfDay - 1]!!
                    }
                }

                mMapFragment.activity?.runOnUiThread {
                    for (index in 0..(routes.size - 2)) {
                        options.add(LatLng(routes[index].terminalDataLatitude?.toDouble()
                                ?: 0.0, routes[index].terminalDataLongitude?.toDouble()
                                ?: 0.0)).zIndex(1f)

                        mMap?.addPolyline(PolylineOptions().width(POLY_LINE_WIDTH)
                                .geodesic(true)
                                .add(LatLng(routes[index].terminalDataLatitude?.toDouble()
                                        ?: 0.0, routes[index].terminalDataLongitude?.toDouble()
                                        ?: 0.0),
                                        LatLng(routes[index + 1].terminalDataLatitude?.toDouble()
                                                ?: 0.0, routes[index + 1].terminalDataLongitude?.toDouble()
                                                ?: 0.0))
                                .color(Color.parseColor(getMotionStateColor(routes[index + 1].motionStateID)))
                                .visible(true)
                                .zIndex(1f))

                        if (index > 0 && routes[index].isPitStopEnd == "1") {
                            val height = 100
                            val width = 100
                            val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 6)
                                    ?: R.drawable.ic_idle)
                            val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                            val markerOptions = MarkerOptions().position(LatLng(routes[index - 1].terminalDataLatitude?.toDouble()
                                    ?: 0.0, routes[index - 1].terminalDataLongitude?.toDouble()
                                    ?: 0.0))
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            markerOptions.snippet(getIdleInfo(routes[index]))

                            idleFlagMarkerOptionList.add(markerOptions)

//                            mMap?.addMarker(markerOptions)?.let { parkingMarker.add(it) }
                        }
                    }
                }

                return options
            }

            override fun onForegound(result: PolylineOptions?) {
                mPolyLine?.remove()
                mRouteStart?.remove()
                mRouteEnd?.remove()

                val firstPosition = mLastVehicleRoute.firstOrNull {
                    it.terminalDataLatitude?.toDoubleOrNull() != null &&
                            it.terminalDataLongitude?.toDoubleOrNull() != null
                }

                firstPosition?.let {
                    val startMarkerOption = MarkerOptions()
                            .position(LatLng(it.terminalDataLatitude?.toDoubleOrNull() ?: 0.0,
                                    it.terminalDataLongitude?.toDoubleOrNull() ?: 0.0))
                            .title("Start")
                            .icon(getStartFlag())
                            .flat(false)
                            .infoWindowAnchor(0.5F, 0.5F)
                            .anchor(0.5f, 0.5f)
                            .rotation(0F)
                    mRouteStart = mMap?.addMarker(startMarkerOption)
                }

                placeVehicleAt(0)

                val endPosition = mLastVehicleRoute.lastOrNull {
                    it.terminalDataLatitude?.toDoubleOrNull() != null &&
                            it.terminalDataLongitude?.toDoubleOrNull() != null
                }

                endPosition?.let {
                    val endMarkerOption = MarkerOptions()
                            .position(LatLng(it.terminalDataLatitude?.toDoubleOrNull() ?: 0.0,
                                    it.terminalDataLongitude?.toDoubleOrNull() ?: 0.0))
                            .title("End")
                            .icon(getEndFlag())
                            .flat(false)
                            .infoWindowAnchor(0.5F, 0.5F)
                            .anchor(0.5f, 0.5f)
                            .rotation(0F)
                    mRouteEnd = mMap?.addMarker(endMarkerOption)
                }

//                mPolyLine = mMap?.addPolyline(result)

                val lastRoute = try {
                    mLastVehicleRoute.lastOrNull {
                        it.terminalDataLatitude?.toDoubleOrNull() != null &&
                                it.terminalDataLongitude?.toDoubleOrNull() != null
                    }
                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    null
                }
                val lat = lastRoute?.terminalDataLatitude?.toDoubleOrNull()
                val lon = lastRoute?.terminalDataLongitude?.toDoubleOrNull()

                val builder = LatLngBounds.builder()
                result?.points?.forEach { builder.include(it) }

                if (lastRoute != null && lat != null && lon != null) {
                    if ((result?.points?.size ?: 0) != 0)
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                }
            }
        })

    }

    private fun addCircle(latlng: LatLng) {
        val options = CircleOptions()
        options.center(latlng)
        options.radius(0.5)
        options.fillColor(ContextCompat.getColor(mMapFragment.requireContext(), R.color.analyticsVehicleRouteIconColor))
        options.strokeColor(ContextCompat.getColor(mMapFragment.requireContext(), R.color.analyticsVehicleRouteIconColor))
//        options.strokePattern(Collections.singletonList(Dot()))
        options.zIndex(1f)

        mMap?.addCircle(options)
    }

    fun getMotionStateColor(motionStateID: String?): String {
        for (item in motionStateVehicleList) {
            if (motionStateID == item.iD) {

                return item.color ?: "#000000"
            }

        }
        return "#000000"
    }

    fun setPolyLineColor(motionStateList: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>?) {
        for (item in motionStateList ?: ArrayList()) {
            motionStateVehicleList.add(item)
        }
    }

    fun placeParkingMarkerOnMap(parkingList: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {

        if (isChecked) {
            parkingMarker.clear()

            for (list in parkingList ?: ArrayList()) {

                if (list.subjectIdentifier == "ACC" && list.actionIdentifier == "ON") {
                    var parking: LatLng? = list.latitude?.toDouble()?.let { list.longitude?.toDouble()?.let { it1 -> LatLng(it, it1) } }

                    val markerOptions = parking?.let { MarkerOptions().position(it) }

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_parking)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

                        markerOptions.snippet(parkingInfoWindow(list))

                        mMap?.addMarker(markerOptions)?.let { parkingMarker.add(it) }
                    }

                }

            }

        } else {
            for (item in parkingMarker) {
                item.remove()
            }

        }

    }

    fun placeSelectedVehicleCurrentLocationOnMap(isVirtualWatchmanValue: Boolean, selectedVehicleLatitude: Double, selectedVehicleLongitude: Double, radiusMeter: Double) {

        if (! isVirtualWatchmanValue) {
            return
        } else {
            var selectedVehicle: LatLng? = selectedVehicleLatitude?.let { selectedVehicleLongitude?.let { it1 -> LatLng(it, it1) } }

            val markerOptions = selectedVehicle?.let { MarkerOptions().position(it) }

            if (markerOptions != null) {

                val height = 100
                val width = 100
                val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_car_engine_off_marker)
                val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                markerOptions.anchor(.5f, .5f)
                //markerOptions.snippet("Radius: ${radiusMeter.toInt()} m")

                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedVehicle, 16f))
                mMap?.addMarker(markerOptions)

            }

        }

    }

    fun addSelectedRadiusCircle(isVirtualWatchmanValue: Boolean, latlng: LatLng, radius: Double) {

        radiusMeter = radius
        isVirtualWatchman = isVirtualWatchmanValue
        selectedLatitude = latlng?.latitude
        selectedLongitude = latlng?.longitude
        var latLng = LatLng(selectedLatitude, selectedLongitude)

        if (! isVirtualWatchman) {
            return
        } else {
            val options = CircleOptions()
            options.center(latLng)
            options.radius(radiusMeter)
            options.fillColor(ContextCompat.getColor(mMapFragment.requireContext(), R.color.circle_transparent_color))
            options.strokeColor(ContextCompat.getColor(mMapFragment.requireContext(), R.color.colorPrimary))
            options.zIndex(1f)
            mMap?.clear()
            mMap?.addCircle(options)
            if (latlng != null) {
                placeSelectedVehicleCurrentLocationOnMap(isVirtualWatchmanValue, latlng.latitude, latlng.longitude, radius)
            }

        }

    }

    fun modifiedItemImage(context: Context, imageId: Int): Int? {
        when (imageId) {
            1 -> {
                return R.drawable.ic_parking
            }
            2 -> {
                return R.drawable.ic_accelaration
            }
            3 -> {
                return R.drawable.ic_harsh_break
            }
            4 -> {
                return R.drawable.ic_speed_violation
            }
            5 -> {
                return R.drawable.ic_engine_start
            }
            6 -> {
                return R.drawable.ic_idle
            }
            7 -> {
                return R.drawable.ic_power_down
            }
            8 -> {
                return R.drawable.ic_engine_stop
            }

            else -> return null
        }

    }

    fun setMarkers(list: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>) {

        val height = 100
        val width = 100

        for (item in list) {

            if (item.subjectIdentifier == "ACC") {
                if (item.actionIdentifier == "ON") {
                    var markerOptions = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                            ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                    val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 1)
                            ?: R.drawable.ic_parking)
                    val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    markerOptions.snippet(parkingInfoWindow(item))

                    parkingMarkerOptions.add(markerOptions)

                    var markerOptions2 = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                            ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                    val bitmap2: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 5)
                            ?: R.drawable.ic_parking)
                    val smallMarker2: Bitmap = Bitmap.createScaledBitmap(bitmap2, width, height, false)

                    markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(smallMarker2))
                    markerOptions2.snippet(getEventInfo(item))

                    engineOnOffMarkerOptions.add(markerOptions2)

                } else {
                    var markerOptions2 = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                            ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                    val bitmap2: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 8)
                            ?: R.drawable.ic_parking)
                    val smallMarker2: Bitmap = Bitmap.createScaledBitmap(bitmap2, width, height, false)

                    markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(smallMarker2))
                    markerOptions2.snippet(getEventInfo(item))

                    engineOnOffMarkerOptions.add(markerOptions2)
                }

            } else if (item.subjectIdentifier == "ACCELERATION") {
                if (item.actionIdentifier == "SUDDEN") {
                    var markerOptions = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                            ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                    val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 2)
                            ?: R.drawable.ic_parking)
                    val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    markerOptions.snippet(getEventInfo(item))

                    suddenAccelerationMarkerOptions.add(markerOptions)

                } else {
                    var markerOptions = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                            ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                    val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 3)
                            ?: R.drawable.ic_parking)
                    val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    markerOptions.snippet(getEventInfo(item))

                    harshBreakMarkerOptions.add(markerOptions)
                }

            } else if (item.subjectIdentifier == "OVERSPEED" && item.actionIdentifier == "COMMIT") {
                var markerOptions = MarkerOptions().position(LatLng(item.latitude?.toDouble()
                        ?: 0.0, item.longitude?.toDouble() ?: 0.0))
                val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, modifiedItemImage(mMapFragment.requireContext(), 4)
                        ?: R.drawable.ic_parking)
                val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                markerOptions.snippet(getEventInfo(item))

                speedViolationMarkerOptions.add(markerOptions)

            } else if (item.subjectIdentifier == "POWER" && item.actionIdentifier == "DOWN") {

            }
        }

    }

    fun parkingInfoWindow(item: VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent): String {

        val eventName = "Event: ${item.subject} ${item.action}"

        val duration = "Duration: ${item.durationTimeAccOff ?: "N/A"}"
        val stoppedTime = "Engine Stopped time: ${item.timeAccOff ?: "N/A"}"
        val startTime = "Engine Started time: ${item.time ?: "N/A"}"

        val geoDistanceMeter: Float = (item.geoLocationDistanceMeter?.toFloat() ?: 1.0F) / 1000.0F

        val location = "Location: $geoDistanceMeter Km / ${getLocationNameFromId(item.geoLocationID ?: "")}"

        return "$eventName\n$duration\n$stoppedTime\n$startTime\n$location"

    }

    fun getEventInfo(item: VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent): String {

        val eventName = "Event: ${item.subject} ${item.action}"
        val time = "Time: ${item.time}"

        val position = "Position: ${item.latitude ?: "0.0"}, ${item.longitude ?: "0.0"}"

        val geoDistanceMeter: Float = (item.geoLocationDistanceMeter?.toFloat() ?: 1.0F) / 1000.0F

        val location = "Location: $geoDistanceMeter Km / ${getLocationNameFromId(item.geoLocationID ?: "")}"

        return "$eventName\n$time\n$location\n$position"

    }

    fun getIdleInfo(item: VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData): String {
        val code = "Code: ${mPrefRepository.currentVehicle()} / ${item.terminalDataID}"

        val vehicle = "Vehicle: ${mPrefRepository.currentVehicleVrn()}"

        val time = "Time: ${item.terminalDataTime}"

        val speed = "Speed: ${item.terminalDataVelocity} Km/H / GPS: ${item.velocityGPSKmH} Km/H"

        val distanceMeter: Float = (item.distanceMeter?.toFloat() ?: 1.0F) / 1000.0F

        val distance = "Distance: $distanceMeter Km/H / ${acumulatedDistanceInMeter / 1000} Km"

        val duration = "Duration: ${secondsToHoursMinutesSeconds(item.durationSecond?.toInt() ?: 0)} / ${secondsToHoursMinutesSeconds(acumulatedDurationInSecond ?: 0)}"

        val position = "Position: ${item.terminalDataLatitude ?: "0.0"}, ${item.terminalDataLongitude ?: "0.0"}"

        val geoDistanceMeter: Float = (item.geoLocationPositionLandmarkDistanceMeter?.toFloat()
                ?: 1.0F) / 1000.0F

        val location = "Location: $geoDistanceMeter Km / ${getLocationNameFromId(item.geoLocationPositionIDLandmark ?: "")}"

        return "$code\n$vehicle\n$time\n$speed\n$distance\n$duration\n$location\n$position"

    }

    private fun secondsToHoursMinutesSeconds(seconds: Int): String {
        if (seconds == 0) {
            return "00:00:00"
        }
        return "${seconds / 3600}:${(seconds % 3600) / 60}:${seconds % 60}"
    }

    private fun getLocationNameFromId(id: String): String {
        if (id == "")
            return ""

        for (item in mCurrentVehicleLocationData)
            if (item.iD == id)
                return item.name ?: ""

        return ""
    }

    fun suddenAccelerationMarkerOnMap(suddenAcceleration: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {
        if (isChecked) {
            suddenAccelerationMarker.clear()

            for (list in suddenAcceleration ?: ArrayList()) {
                if (list.subjectIdentifier == "ACCELERATION" && list.actionIdentifier == "SUDDEN") {
                    var acceleration: LatLng? = LatLng(list.latitude?.toDouble()
                            ?: 0.0, list.longitude?.toDouble() ?: 0.0)

                    val markerOptions = acceleration?.let { MarkerOptions().position(it) }

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_accelaration)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        markerOptions.snippet(getEventInfo(list))

                        mMap?.addMarker(markerOptions)?.let { suddenAccelerationMarker.add(it) }

                    }

                }
            }

        } else {
            for (item in suddenAccelerationMarker) {
                item.remove()
            }
        }
    }

    fun harshBreakMarkerOnMap(harshBreak: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {
        if (isChecked) {
            harshBreakMarker.clear()

            for (list in harshBreak ?: ArrayList()) {
                if (list.subjectIdentifier == "ACCELERATION" && list.actionIdentifier == "BRAKE") {
                    var harshBreakLatLong: LatLng? = LatLng(list.latitude?.toDouble()
                            ?: 0.0, list.longitude?.toDouble() ?: 0.0)

                    val markerOptions = if (harshBreakLatLong != null) {
                        MarkerOptions().position(harshBreakLatLong)
                    } else null

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val bitmap: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_harsh_break)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        markerOptions.snippet(getEventInfo(list))

                        mMap?.addMarker(markerOptions)?.let { harshBreakMarker.add(it) }

                    }

                    //harshBreakMarker.add(mMap?.addMarker(markerOptions))

                }
            }

        } else {
            for (item in harshBreakMarker) {
                item.remove()
            }
        }
    }

    fun speedViolationMarkerOnMap(speedViolation: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {
        if (isChecked) {
            speedViolationMarker.clear()

            for (list in speedViolation ?: ArrayList()) {
                if (list.subjectIdentifier == "OVERSPEED" && list.actionIdentifier == "COMMIT") {
                    var speedViolationLatLong: LatLng? = LatLng(list.latitude?.toDouble()
                            ?: 0.0, list.longitude?.toDouble() ?: 0.0)

                    val markerOptions = if (speedViolationLatLong != null) {
                        MarkerOptions().position(speedViolationLatLong)
                    } else null

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val b: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_engine_start)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        markerOptions.snippet(getEventInfo(list))

                        mMap?.addMarker(markerOptions)?.let { speedViolationMarker.add(it) }
                    }

                }
            }

        } else {
            for (item in speedViolationMarker) {
                item.remove()
            }
        }
    }

    fun placeEngineOnOffMarkerOnMap(engineOnOffList: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {
        if (isChecked) {
            engineOnOffMarker.clear()
            for (list in engineOnOffList ?: ArrayList()) {

                if (list.subjectIdentifier == "ACC" && list.actionIdentifier == "ON" || list.actionIdentifier == "OFF") {
                    var engineOnOff: LatLng? = list.latitude?.toDouble()?.let { list.longitude?.toDouble()?.let { it1 -> LatLng(it, it1) } }

                    val markerOptions = engineOnOff?.let { MarkerOptions().position(it) }

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val b: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_engine_start)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        markerOptions.snippet(getEventInfo(list))

                        mMap?.addMarker(markerOptions)?.let { engineOnOffMarker.add(it) }
                    }
                }

            }

        } else {
            for (item in engineOnOffMarker) {
                item.remove()
            }
        }
    }

    fun placePowerDownMarkerOnMap(powerDownList: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>?, isChecked: Boolean) {
        if (isChecked) {
            powerDownMarker.clear()
            for (list in powerDownList ?: ArrayList()) {
                if (list.subjectIdentifier == "POWER" && list.actionIdentifier == "DOWN") {
                    var powerDownLatLong: LatLng? = LatLng(list.latitude?.toDouble()
                            ?: 0.0, list.longitude?.toDouble() ?: 0.0)

                    val markerOptions = if (powerDownLatLong != null) {
                        MarkerOptions().position(powerDownLatLong)
                    } else null

                    if (markerOptions != null) {
                        val height = 100
                        val width = 100
                        val b: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_power_down)
                        val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, width, height, false)

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        markerOptions.snippet(getEventInfo(list))

                        mMap?.addMarker(markerOptions)?.let { powerDownMarker.add(it) }
                    }
                }

            }
        } else {
            for (item in powerDownMarker) {
                item.remove()
            }
        }
    }

    fun placeCurrentLocationMarkerOnMap(latlng: LatLng){

        val markerOptions = MarkerOptions().position(latlng)

        if (markerOptions != null) {
            val height = 100
            val width = 100
            val b: Bitmap = BitmapFactory.decodeResource(mMapFragment.context?.resources, R.drawable.ic_current_location_marker)
            val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, width, height, false)

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchor(0.5f, 0.5f)

            mMap?.addMarker(markerOptions)

            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))

        }
    }

    fun toggleParkingFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in parkingMarkerOptions)
                mMap?.addMarker(marker)?.let { parkingMarker.add(it) }

        } else {
            for (marker in parkingMarker)
                marker.remove()
        }
    }

    fun toggleSuddenFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in suddenAccelerationMarkerOptions)
                mMap?.addMarker(marker)?.let { suddenAccelerationMarker.add(it) }

        } else {
            for (marker in suddenAccelerationMarker)
                marker.remove()
        }
    }

    fun toggleHarshFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in harshBreakMarkerOptions)
                mMap?.addMarker(marker)?.let { harshBreakMarker.add(it) }

        } else {
            for (marker in harshBreakMarker)
                marker.remove()
        }
    }

    fun toggleViolationFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in speedViolationMarkerOptions)
                mMap?.addMarker(marker)?.let { speedViolationMarker.add(it) }

        } else {
            for (marker in speedViolationMarker)
                marker.remove()
        }
    }

    fun toggleEngineFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in engineOnOffMarkerOptions)
                mMap?.addMarker(marker)?.let { engineOnOffMarker.add(it) }

        } else {
            for (marker in engineOnOffMarker)
                marker.remove()
        }
    }

    fun toggleIdleFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in idleFlagMarkerOptionList)
                mMap?.addMarker(marker)?.let { idleFlagMarkerList.add(it) }

        } else {
            for (marker in idleFlagMarkerList)
                marker.remove()
        }
    }

    fun togglePowerFlagMarker(isChecked: Boolean) {
        if (isChecked) {
            for (marker in powerDownMarkerOptions)
                mMap?.addMarker(marker)?.let { powerDownMarker.add(it) }

        } else {
            for (marker in powerDownMarker)
                marker.remove()
        }
    }

    fun fetchVehicleRouteAt(time: Int): VehicleRoute? {
        val route = mRoutesByTime.get(time)

        return route
    }

    fun placeVehicleAt(time: Int, animate: Boolean = false) {
        val giveTimeLocation = mLatLngByTime[time]

        if (time == 0 && mLastVehicleRoute.isNotEmpty()) {
            val firstPosition = mLastVehicleRoute.first()

            val markerOption = MarkerOptions().position(LatLng(firstPosition.terminalDataLatitude?.toDouble()
                    ?: 0.0,
                    firstPosition.terminalDataLongitude?.toDouble() ?: 0.0))
                    .title(mRoutesByTime[time]?.location?.place)
                    .icon(getBitmapDescriptor())
//                    .icon(getVehicleIcon(mRoutesByTime[time]?.bstId.toString()))
                    .flat(true)
                    .infoWindowAnchor(0.5F, 0.5F)
                    .anchor(0.5f, 0.5f)
                    .rotation(mRoutesByTime[time]?.location?.direction?.toFloatOrNull() ?: 0.0f)

            if (mMarker == null) {
                mMarker = mMap?.addMarker(markerOption)
            }
            return

        } else if (giveTimeLocation == null) {
            val icon = getGreyBitmapDescriptor()
            mMarker?.setIcon(icon)
            return
        }

        val previousTimeLocation = mLatLngByTime[time - 1] ?: mLatLngByTime[time] !!

        val icon = getBitmapDescriptor() // getVehicleIcon(mRoutesByTime[time]?.bstId.toString())

        val snippetString = StringBuilder()
                .apply {
                    this.append("Near: ")
                    this.append(mRoutesByTime[time]?.location?.place ?: "--")
                    this.append("; ")
                    this.append("Vehicle ID: ")
                    this.append(mRoutesByTime[time]?.bstId?.toUpperCase())
                    this.append("; ")
                    this.append("Time: ")
                    this.append(mRoutesByTime[time]?.updatedAt ?: "--")
                    this.append("; ")
                    this.append("Engine: ")
                    this.append(mRoutesByTime[time]?.engineStatus ?: "--")
                    this.append("; ")
                    this.append("Speed: ")
                    this.append(mRoutesByTime[time]?.speed ?: "--")
                    this.append(" KM/H")
                }
                .toString()
        val markerOption = MarkerOptions().position(giveTimeLocation)
                .title(mRoutesByTime[time]?.location?.place)
                .snippet(snippetString)
                .icon(icon)
                .flat(true)
                .infoWindowAnchor(0.5F, 0.5F)
                .anchor(0.5f, 0.5f)
                .rotation(mRoutesByTime[time]?.location?.direction?.toFloatOrNull() ?: 0.0f)

        if (mMarker == null) {
            mMarker = mMap?.addMarker(markerOption)
        } else {
            if (mMarker?.isInfoWindowShown == true && snippetString != mMarker?.snippet) {
                mMarker?.hideInfoWindow()
                mMarker?.showInfoWindow()
            }
            // mMarker?.snippet = snippetString
            mMarker?.title = mRoutesByTime[time]?.location?.place
            mMarker?.setIcon(icon)

            val direction = getAngle(giveTimeLocation, previousTimeLocation) // + 90
            animateMarkerForVehicleAnalytics(giveTimeLocation, mRoutesByTime[time]?.updatedAtDate(), mMarker, direction)
        }

        var bounds = mMap?.projection?.visibleRegion?.latLngBounds ?: return

        bounds = reduceLatLngBoundBy(bounds, 0.5)

        val deltaDistance = SphericalUtil.computeDistanceBetween(giveTimeLocation, bounds.center)

        if (! bounds.contains(giveTimeLocation) || deltaDistance >= 2 * 1000) {
            val cameraUpdate = CameraUpdateFactory.newLatLng(giveTimeLocation)

            if (animate) {
                mMap?.animateCamera(cameraUpdate)
            } else {
                mMap?.moveCamera(cameraUpdate)
            }
        }
    }

    private fun getBitmapDescriptor(): BitmapDescriptor? {
        if (mMapFragment.context == null)
            return null
        var carIcon = BitmapFactory.decodeResource(mMapFragment.context?.resources,
                R.drawable.ic_car_icon)
        carIcon = bitmapSizeByScall(carIcon, 0.5f)
//        carIcon = bitmapSizeByScall(carIcon, 1.4f)
        val matrix = Matrix()
        matrix.postRotate(90F)
        val rotated = Bitmap.createBitmap(carIcon, 0, 0, carIcon.width, carIcon.height, matrix, true)
        val icon = BitmapDescriptorFactory.fromBitmap(rotated)
        return icon
                ?: BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(mMapFragment.context?.resources,
                                R.drawable.ic_car_icon))
    }

    private fun getVehicleIcon(bstId: String): BitmapDescriptor? {
        if (mMapFragment.context == null)
            return null

        var carIcon = setEngineOffMarker(mMapFragment.requireContext(), "")

        for (item in allVehiclesDataList) {
            if (bstId == item.bstId) {
                val lastUpdatedDate = item.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")
                val updatedDate = try {
                    DateTime.parse(lastUpdatedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                } catch (ex: Exception) {
                    null
                }

                val carrierTypeName = item.carrierTypeName.toString().toLowerCase(Locale.ROOT)

                carIcon = if (updatedDate?.isBefore(DateTime.now().minusDays(1)) == true)
                    setDisableMarker(mMapFragment.requireContext(), carrierTypeName)
                else if (item.terminalDataIsAccOnLast == "1" && item.terminalDataVelocityLast?.toDouble() ?: 0.0 == 0.0)
                    setIdleMarker(mMapFragment.requireContext(), carrierTypeName)
                else if (item.terminalDataIsAccOnLast == "1")
                    setMovingMarker(mMapFragment.requireContext(), carrierTypeName)
                else
                    setEngineOffMarker(mMapFragment.requireContext(), carrierTypeName)

                break
            }
        }

        carIcon = bitmapSizeByScall(carIcon, 1.4f)
        val rotated = Bitmap.createBitmap(carIcon, 0, 0, carIcon.width, carIcon.height, Matrix(), true)
        return BitmapDescriptorFactory.fromBitmap(rotated)
    }

    private fun getStartFlag(): BitmapDescriptor? {
        if (mMapFragment.context == null)
            return null
        var carIcon = BitmapFactory.decodeResource(mMapFragment.context?.resources,
                R.drawable.flag_green)
        carIcon = bitmapSizeByScall(carIcon, 0.2f)
        val icon = BitmapDescriptorFactory.fromBitmap(carIcon)
        return icon
                ?: BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(mMapFragment.context?.resources,
                                R.drawable.flag_green))
    }

    private fun getEndFlag(): BitmapDescriptor? {
        if (mMapFragment.context == null)
            return null
        var carIcon = BitmapFactory.decodeResource(mMapFragment.context?.resources,
                R.drawable.flag_red)
        carIcon = bitmapSizeByScall(carIcon, 0.2f)
        val icon = BitmapDescriptorFactory.fromBitmap(carIcon)
        return icon
                ?: BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(mMapFragment.context?.resources,
                                R.drawable.flag_red))
    }

    private fun getGreyBitmapDescriptor(): BitmapDescriptor? {
        if (mMapFragment.context == null)
            return null
        var carIcon = BitmapFactory.decodeResource(mMapFragment.context?.resources,
                R.drawable.ic_car_icon_disabled)
        carIcon = bitmapSizeByScall(carIcon, 0.5f)
        val matrix = Matrix()
        matrix.postRotate(90F)
        val rotated = Bitmap.createBitmap(carIcon, 0, 0, carIcon.width, carIcon.height, matrix, true)
        val icon = BitmapDescriptorFactory.fromBitmap(rotated)
        return icon
                ?: BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(mMapFragment.context?.resources,
                                R.drawable.ic_car_icon_disabled))
    }

    private fun animateMarker(destination: LatLng, lastTime: DateTime?, marker: Marker?, direction: Float) {
        if (marker != null) {
            var animationDuration: Long = 1000
            if (lastTime != null) {
                animationDuration = calculateAnimationTime(lastTime)
            }

            val startPosition = marker.position
            val endPosition = LatLng(destination.latitude, destination.longitude)

            val startRotation = marker.rotation

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = animationDuration
            valueAnimator.interpolator = AccelerateDecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    marker.position = newPosition
                    marker.rotation = computeRotation(v, startRotation, direction)

                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }
            }

            valueAnimator.start()
        }
    }

    private fun animateMarkerForVehicleAnalytics(destination: LatLng, lastTime: DateTime?, marker: Marker?, direction: Float) {
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(destination.latitude, destination.longitude)

            val startRotation = marker.rotation

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 100
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    marker.position = newPosition
                    marker.rotation = computeRotation(v, startRotation, direction)

                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }
            }

            valueAnimator.start()
        }
    }

    private fun calculateAnimationTime(lastTime: DateTime): Long {
        val timeDifference = DateTime.now().millis - lastTime.millis
        val animationTime = ceil(timeDifference.div(10).toDouble()).toLong()
        return if (animationTime >= 10000) 10000 else animationTime
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start
        val normalizedEndAbs = (normalizeEnd + 360) % 360

        val direction = (if (normalizedEndAbs > 180) - 1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        if (direction > 0) {
            rotation = normalizedEndAbs
        } else {
            rotation = normalizedEndAbs - 360
        }

        val result = fraction * rotation + start
        return (result + 360) % 360
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    fun isRouteDataAvailable(): Boolean {
        return mLastRoute.isNotEmpty()
    }

    fun isVehicleRouteDataAvailable(): Boolean {
        return mLastVehicleRoute.isNotEmpty()
    }

    /**
     * toHome : if update is required when home button is pressed
     */

    fun placeMarkerAtCurrentPosition(vehicleStatus: VehicleStatus?, toHome: Boolean = false,
                                     isFirstTime: Boolean = false) {

        val lastVehicleStatus = mCurrentVehicleStatus
        mCurrentVehicleStatus = vehicleStatus

        val shouldClearTrail =
                (lastVehicleStatus != null && lastVehicleStatus.updateAtTime()?.isBefore(
                        mCurrentVehicleStatus?.updateAtTime()?.minusMinutes(1)) == true) ||
                        mCurrentVehicleStatus?.updateAtTime()?.isBefore(
                                DateTime.now().minusMinutes(2)) == true

       if (vehicleStatus?.location?.latitude.isNullOrEmpty() || vehicleStatus?.location?.longitude.isNullOrEmpty()) {
            return
        }

        if (lastVehicleStatus != null && lastVehicleStatus.engineStatus == "OFF" && !isFirstTime) {
            return
        }
      if (vehicleStatus == null || ((lastVehicleStatus?.location?.latitude == vehicleStatus.location?.latitude
                        && lastVehicleStatus?.location?.longitude == vehicleStatus.location?.longitude
                        ) && !isFirstTime)) {
            return
        }

        val currentLocation = LatLng(
            vehicleStatus?.location?.latitude?.toDoubleOrNull()
                ?: 0.0, vehicleStatus?.location?.longitude?.toDoubleOrNull() ?: 0.0)
        val vehicleIcon = getVehicleIcon(vehicleStatus!!.bstid)

        //marker click location

        val snippetString = StringBuilder()
                .apply {
                    this.append("Near Of: ")
                    this.append(vehicleStatus?.location?.place ?: "--")
                    this.append("; ")
                    this.append("Vehicle ID: ")
                    this.append(vehicleStatus?.bstid?.toUpperCase())
                    this.append("; ")
                    this.append("Time: ")
                    this.append(vehicleStatus?.updatedAt ?: "--")
                    this.append("; ")
                    this.append("Engine: ")
                    this.append(vehicleStatus?.engineStatus ?: "--")
                    this.append("; ")
                    this.append("Speed: ")
                    this.append(vehicleStatus?.speed ?: "--")
                    this.append(" KM/H")
                }
                .toString()

        val vehicleMarker = MarkerOptions().position(currentLocation)
                .title(vehicleStatus.location?.place)
                .snippet(snippetString)
                .icon(vehicleIcon)
                .flat(true)
                .anchor(0.5f, 0.5f)

        var lastLocation = LatLng(0.0, 0.0)
        var heading = 0.0f

        if (lastVehicleStatus != null) {
            lastLocation = LatLng(lastVehicleStatus.location?.latitude?.toDoubleOrNull()
                    ?: 0.0, lastVehicleStatus.location?.longitude?.toDoubleOrNull() ?: 0.0)

            heading = SphericalUtil.computeHeading(lastLocation, currentLocation).toFloat()

            vehicleMarker.rotation(heading - 90.0F)
        }

        if (mMarker == null) {
            mMarker = mMap?.addMarker(vehicleMarker)

        } else {
            mMarker?.title = vehicleStatus.location?.place
            val location = LatLng(lastVehicleStatus?.location?.latitude?.toDoubleOrNull()
                    ?: 0.0,
                    lastVehicleStatus?.location?.latitude?.toDoubleOrNull() ?: 0.0)
            val direction = getAngle(location, currentLocation)
            animateMarker(currentLocation, lastVehicleStatus?.updateAtTime(), mMarker, vehicleMarker.rotation)
            if (mMarker?.isInfoWindowShown == true && snippetString != mMarker?.snippet) {
                mMarker?.hideInfoWindow()
//             mMarker?.showInfoWindow()
            }
            mMarker?.snippet = snippetString
            mMarker?.title = vehicleStatus.location?.place
            mMarker?.setIcon(vehicleIcon)
            mMarker?.zIndex = 1f


        }

        var bounds: LatLngBounds = mMap?.projection?.visibleRegion?.latLngBounds ?: return

        bounds = reduceLatLngBoundBy(bounds, 0.5)

        if (lastVehicleStatus != null) {
            var arrowIcon = BitmapFactory.decodeResource(mMapFragment.context?.resources,
                    R.drawable.ic_map_arrow)
            arrowIcon = bitmapSizeByScall(arrowIcon, 0.5f)
            val icon = BitmapDescriptorFactory.fromBitmap(arrowIcon)

            val markerOption = MarkerOptions().position(lastLocation)
                    .title(lastVehicleStatus.location?.place)
                    .icon(icon)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .rotation(heading)

            val trailMarker = mMap?.addMarker(markerOption)

            trailMarker?.setIcon(icon)
            trailMarker?.zIndex = 1f
        }

        val polypoints = mPolyLine?.points

        val options = PolylineOptions()
                .width(POLY_LINE_WIDTH)
                .color(ContextCompat.getColor(mMapFragment.requireContext(), R.color.colorAccent))
                .geodesic(true)
                .visible(true)
                .zIndex(1f)

        if (polypoints != null) {
            options.addAll(polypoints)
        }
        options.add(currentLocation)
        mPolyLine = mMap?.addPolyline(options)

        val deltaDistance = SphericalUtil.computeDistanceBetween(currentLocation, bounds.center)

        if (! bounds.contains(
                        currentLocation) && (toHome || deltaDistance > 10 * 1000) || isFirstTime) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)

            mMap?.moveCamera(cameraUpdate)
        }
    }


    private fun addClusterItems() {
        allVehiclesLatLngDataList.clear()

        for (index in allVehiclesDataList.indices) {
            val item = allVehiclesDataList[index]
            val lat = item.terminalDataLatitudeLast?.toDouble() ?: 0.0
            val lng = item.terminalDataLongitudeLast?.toDouble() ?: 0.0

            val latLng = LatLng(lat, lng)
            val title = ""
            val snippet = vehicleSnippetInfoWindow(item)
            val offsetItem = MyClusterItem(latLng, title, snippet, index)
            allVehiclesLatLngDataList.add(offsetItem)
        }

        Log.e("TAG", "addClusterItems: ${allVehiclesLatLngDataList.size}")
        mClusterManager?.clearItems()
        mClusterManager?.addItems(allVehiclesLatLngDataList)
        mClusterManager?.cluster()
    }

    private fun vehicleSnippetInfoWindow(item: Terminal): String? {

        var distanceMeter = item.geoLocationPositionLandmarkDistanceMeter
        var near = "${item.geoLocationName} ($distanceMeter m)"
        var vrn = item.vrn
        var time = item.terminalDataTimeLast?.toString(
                "yyyy-MM-dd HH:mm:ss")
        var engine = if (item.terminalDataIsAccOnLast == "1") "ON" else "OFF"
        var speed = "${item.terminalDataVelocityLast} KM/H"

        return "Near: $near\nVRN: $vrn\nTime: $time\nEngine: $engine\nSpeed: $speed"
    }

    private fun smallMarkerForAllVehicles(index: Int): BitmapDescriptor? {

        lateinit var carIcon: Bitmap

        var icon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(mMapFragment.context?.resources,
                        R.drawable.ic_car_icon_disabled))
        val item = allVehiclesDataList[index]

        val lastUpdatedDate = item.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")

        var updatedDate = try {
            DateTime.parse(lastUpdatedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (ex: Exception) {
            null
        }

        var carrierTypeName = item?.carrierTypeName.toString().toLowerCase()

        if (updatedDate?.isBefore(DateTime.now().minusDays(1)) == true) {

            carIcon = setDisableMarker(mMapFragment?.requireContext(), carrierTypeName)

            carIcon = bitmapSizeByScall(carIcon, 1.4f)
            icon = BitmapDescriptorFactory.fromBitmap(carIcon)
            return icon

        } else if (item.terminalDataIsAccOnLast == "1" && item.terminalDataVelocityLast?.toDouble() ?: 0.0 == 0.0) {

            carIcon = setIdleMarker(mMapFragment?.requireContext(), carrierTypeName)

            carIcon = bitmapSizeByScall(carIcon, 1.4f)
            icon = BitmapDescriptorFactory.fromBitmap(carIcon)
            return icon

        } else if (item.terminalDataIsAccOnLast == "1") {

            carIcon = setMovingMarker(mMapFragment?.requireContext(), carrierTypeName)

            carIcon = bitmapSizeByScall(carIcon, 1.4f)
            icon = BitmapDescriptorFactory.fromBitmap(carIcon)
            return icon

        }

        carIcon = setEngineOffMarker(mMapFragment?.requireContext(), carrierTypeName)

        carIcon = bitmapSizeByScall(carIcon, 1.4f)
        icon = BitmapDescriptorFactory.fromBitmap(carIcon)
        return icon
    }

    fun removePolyLine() {
        mPolyLine = null
        mLastRoute = ArrayList()
        mCurrentVehicleStatus = null
        mRoutesByTime = HashMap()
        mLatLngByTime = HashMap()
        mPolyLine?.remove()
        mMarker = null
        mMap?.clear()
    }

    fun clearMap() {
        Log.e("TAG", "clearMap: Called")
        mCurrentVehicleStatus = null
        mMarker = null
        mPolyLine = null
        mMap?.clear()
    }

    fun placeFirstValue() {
        for (i in 0 .. mLatLngByTime.size) {
            if (mLatLngByTime.get(i) != null) {
                placeVehicleAt(i, true)
                break
            }
        }
    }

    fun reduceLatLngBoundBy(bounds: LatLngBounds, percentage: Double): LatLngBounds {
        val distance = SphericalUtil.computeDistanceBetween(bounds.northeast, bounds.southwest)
        val reduced = distance * percentage

        val headingNESW = SphericalUtil.computeHeading(bounds.northeast, bounds.southwest)
        val newNE = SphericalUtil.computeOffset(bounds.northeast, reduced / 2.0, headingNESW)

        val headingSWNE = SphericalUtil.computeHeading(bounds.southwest, bounds.northeast)
        val newSW = SphericalUtil.computeOffset(bounds.southwest, reduced / 2.0, headingSWNE)

        return LatLngBounds.builder().include(newNE).include(newSW).build()
    }

    fun focusOnCurrentStatus() {
        if (mMap == null || mCurrentVehicleStatus == null)
            return
        if (mCurrentVehicleStatus?.location?.longitude.isNullOrEmpty()
                || mCurrentVehicleStatus?.location?.latitude.isNullOrEmpty()) {
            return
        }
        val latLng = LatLng(mCurrentVehicleStatus?.location?.latitude?.toDouble()
                ?: 0.toDouble(), mCurrentVehicleStatus?.location?.longitude?.toDouble()
                ?: 0.toDouble())
        mMarker?.remove()
        //val icon = getBitmapDescriptor()
        val icon = getVehicleIcon(mCurrentVehicleStatus?.bstid.toString())
        val snippetString = StringBuilder()
                .apply {
                    this.append("Near: ")
                    this.append(mCurrentVehicleStatus?.location?.place ?: "--")
                    this.append("; ")
                    this.append("VRN: ")
                    this.append(mCurrentVehicleStatus?.vrn)
                    this.append("; ")
                    this.append("Time: ")
                    this.append(mCurrentVehicleStatus?.updatedAt ?: "--")
                    this.append("; ")
                    this.append("Engine: ")
                    this.append(mCurrentVehicleStatus?.engineStatus ?: "--")
                    this.append("; ")
                    this.append("Speed: ")
                    this.append(mCurrentVehicleStatus?.speed ?: "--")
                    this.append(" KM/H")
                }
                .toString()
        val markerOption = MarkerOptions().position(latLng)
                .title(mCurrentVehicleStatus?.location?.place)
                .snippet(snippetString)
                .icon(icon)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .infoWindowAnchor(0.5F, 0.5F)
                .rotation(mCurrentVehicleStatus?.location?.direction?.toFloatOrNull() ?: 0.0f)
        mMarker = mMap?.addMarker(markerOption)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

    }

    fun focusOnCurrentVehicle(vehicleStatus: VehicleStatus) {

        if(vehicleStatus.location == null) return

        val latlon = LatLng(
                vehicleStatus.location.latitude?.toDoubleOrNull() ?: 0.0,
                vehicleStatus.location.longitude?.toDoubleOrNull() ?: 0.0
        )
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlon, 15f))
    }

    fun focusOnLocation(location: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    fun focusOnCurrentStatusFromVehicleMonitoring(event: CurrentVehicleChangeEvent) {
        for (data in allVehiclesDataList) {
            if (event.bstId == data.bstId) {
                if (mMap == null)
                    return
                if (data.terminalDataLatitudeLast.isNullOrEmpty()
                        || data.terminalDataLongitudeLast.isNullOrEmpty()) {
                    return
                }
                val latLng = LatLng(data.terminalDataLatitudeLast?.toDouble()
                        ?: 0.toDouble(), data.terminalDataLongitudeLast?.toDouble()
                        ?: 0.toDouble())

//                mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))

                break
            }
        }

    }

    fun selectedVehicleLatLng(event: CurrentVehicleChangeEvent): LatLng {
        var vehicleLatLng: LatLng = LatLng(0.00, 0.00)
        for (data in allVehiclesDataList) {

            if (event.bstId == data.bstId) {
                if (mMap == null || data == null)
                    return LatLng(0.00, 0.00)
                if (data?.terminalDataLatitudeLast.isNullOrEmpty()
                        || data?.terminalDataLongitudeLast.isNullOrEmpty()) {
                    return LatLng(0.00, 0.00)
                }
                vehicleLatLng = LatLng(data?.terminalDataLatitudeLast?.toDouble()
                        ?: 0.toDouble(), data?.terminalDataLongitudeLast?.toDouble()
                        ?: 0.toDouble())
            }
        }

        return vehicleLatLng

    }

    fun setTraffic(boolean: Boolean) {
        mMap?.isTrafficEnabled = boolean
    }

    fun currentVehicleStatus(): VehicleStatus? {
        return mCurrentVehicleStatus
    }

    override fun onClusterClick(p0: Cluster<MyClusterItem>?): Boolean {
        onMapClicked?.onClicked(true)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(p0?.position, 14f))
        return true
    }

    // Commented for future reference
    /*override fun onClusterItemClick(p0: MyClusterItem?): Boolean {
        Log.e("kjm", "entered 2")
        isClusterOnClicked = true
        val model = allVehiclesDataList[p0?.index ?: 0]
        mPrefRepository.changeCurrentVehicle(model.bstid, model.vrn, model.bid)

        return false
    }*/

    override fun onClusterItemClick(p0: MyClusterItem?): Boolean {
        onMapClicked?.onClicked(true)
        isClusterOnClicked = true
        val model = allVehiclesDataList[p0?.index ?: 0]
//        mPrefRepository.changeCurrentVehicle(model.bstid, model.vrn, model.bid)
        mPrefRepository.changeCurrentVehicle(model.bstid, model.vrn, model.bid, "${model.terminalDataLatitudeLast},${model.terminalDataLongitudeLast}")
        return false
    }

    private fun updateVehicle(model: Terminal) {
        val dialogFrag = BottomNavFragment.newInstance()
        dialogFrag.updateVehicle(model)
        mVehicleViewModel.changeCurrentVehicle(model.bstid, model.vrn, model.bid)
        mPrefRepository.changeCurrentVehicle(model.bstid, model.vrn, model.bid, "${model.terminalDataLatitudeLast},${model.terminalDataLongitudeLast}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        mMapFragment.activity?.runOnUiThread {
            updateVehicle()
        }
    }

    private fun updateVehicle() {
        val currentVehicle = mVehicleViewModel.mPrefRepository.currentVehicle()
        val currentVehicleVrn = mVehicleViewModel.mPrefRepository.currentVehicleVrn()

        mVehicleViewModel.getCurrentVehicle {
            val location = LatLng((it?.terminalDataLatitudeLast
                    ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
        }

    }

    override fun onMapClick(p0: LatLng?) {
        onMapClicked?.onClicked(true)
    }
}