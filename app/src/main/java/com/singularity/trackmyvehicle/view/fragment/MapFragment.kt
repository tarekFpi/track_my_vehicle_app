package com.singularity.trackmyvehicle.view.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleCurrentStatusModel
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.model.event.NetworkConnectivityEvent
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.OnItemClickListener
import com.singularity.trackmyvehicle.repository.interfaces.OnMapClicked
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.view.activity.*
import com.singularity.trackmyvehicle.view.activity.MonitoringActivity.Companion.LOCATION_PERMISSION
import com.singularity.trackmyvehicle.view.adapter.ReportButtonAdapter
import com.singularity.trackmyvehicle.view.adapter.ReportButtonModel
import com.singularity.trackmyvehicle.view.adapter.VehicleCurrentStatusAdapter
import com.singularity.trackmyvehicle.view.customview.behaviour.BottomSheetBehaviorGoogleMapsLike
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendExpiredDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendedMaintenanceDialog
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.VehicleMonitoringViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnItemClickCallback<ReportButtonModel> {

    @Inject
    lateinit var mPrefRepository: PrefRepository

    @Inject
    lateinit var mVehicleRepository: VehicleRepository

    @Inject
    lateinit var mNetworkAvailabilityChecker: NetworkAvailabilityChecker

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    private val LOCATION_REFRESH_TIME = 5000

    private var userLocation: LatLng? = null
    private var isFocusing = false
    private var focusRunnable: Runnable = Runnable {
        mMapDrawer?.focusOnCurrentVehicle((activity as MonitoringActivity).currentVehicleLocation)
        isFocusing = false
    }
    private var focusHandler: Handler = Handler(Looper.getMainLooper())

    private var mCurrentDistanceTravelLiveData: LiveData<TerminalAggregatedData>? = null
    private var menuItemId: Int = -1
    private var allVehicles = ArrayList<Terminal>()
    private var movingVehicles = ArrayList<Terminal>()
    private var offlineVehicles = ArrayList<Terminal>()
    private var idleVehicles = ArrayList<Terminal>()
    private var engineOffVehicles = ArrayList<Terminal>()

    var VEHICLE_TYPE = ALL_VEHICLES_TYPE

    private var isShowAllVehicle = false
    var isShowMonitor = true
    private var isMapStyleDefault = true
    private var isMapModeLight = true
    var isContainerViewHidden = true

    var isFirstTime = true

    private var mDialog: MaterialDialog? = null

    var vehicleStatusData: VehicleStatus? = null

    var mMapDrawer: MapDrawer? = null
    val suspendExpiredDialog = SuspendExpiredDialog()

    val suspendMaintenanceDialog = SuspendedMaintenanceDialog()


    var lastLocation = VehicleStatus()

    private lateinit var appPreference : AppPreference

    val suspendDialog = SuspendDialog()
    lateinit var suspendView: RelativeLayout
    lateinit var blurView: ImageView

    private lateinit var monitorViewModel: VehicleMonitoringViewModel
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var adapter: VehicleCurrentStatusAdapter

    private val mCurrentDistanceTravelObserver = Observer<TerminalAggregatedData> { data ->
        if (data == null) {
            txtTravelledDistance.text = "--"
            progressTravelledDistance.visibility = View.GONE
            return@Observer
        }
        progressTravelledDistance.visibility = View.GONE

        val distanceInMeter: Float = (data.terminalDataMinutelyDistanceMeter?.toFloatOrNull()
                ?: 0.0F)
        txtTravelledDistance.text = String.format("%.1f KM", distanceInMeter / 1000)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        suspendView = view.findViewById<RelativeLayout>(R.id.suspendView)
        blurView = view.findViewById<ImageView>(R.id.blur_view)
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VehicleTrackApplication.appComponent?.inject(this)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        monitorViewModel = ViewModelProvider(this).get(VehicleMonitoringViewModel::class.java)
    }

    private fun selectNextVehicle() {
        mVehicleViewModel.selectNextVehicle()
    }

    private fun selectPreviousVehicle() {
        mVehicleViewModel.selectPreviousVehicle()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_map, container, false)


        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fabDirection.setOnClickListener {
            val currentVehicleStatus = (activity as MonitoringActivity).currentVehicleLocation
            val lat = currentVehicleStatus.location.latitude?.toDoubleOrNull() ?: 0.0
            val lon = currentVehicleStatus.location.longitude?.toDoubleOrNull() ?: 0.0

            if (lat != 0.0 && lon != 0.0) {
                val url = "https://www.google.com/maps/dir/?api=1&origin=$lat,$lon&travelmode=driving"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)

            } else {
                context?.let { it1 -> Toasty.error(it1, "Location not available").show() }
            }
        }

        virtualWatchman.setOnClickListener {
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                Toast.makeText(it.context, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val terminalId = mPrefRepository.currentVehicleTerminalId
            if (terminalId == null || terminalId == "" || terminalId == "null") {
                //return@setOnClickListener
                Toast.makeText(it.context, "Terminal id not found, Please try again", Toast.LENGTH_SHORT).show()

            } else {
                val intent = Intent(it.context, VirtualWatchmanSet::class.java)
                intent.putExtra("Terminal_ID", terminalId)
                val location: List<String> = try {
                    mPrefRepository.currentLocation().split(",")
                } catch (e: java.lang.Exception) {
                    listOf("0.0", "0.0")
                }
                intent.putExtra("Selected_Vehicle_Latitude", location[0].toDoubleOrNull() ?: 0.0)
                intent.putExtra("Selected_Vehicle_Longitude", location[1].toDoubleOrNull() ?: 0.0)
                startActivity(intent)
            }
        }

        defaultMapView.setOnClickListener {
            if (isMapStyleDefault) return@setOnClickListener

            isMapStyleDefault = true
            updateMapStyle()
        }

        satelliteMapView.setOnClickListener {
            if (!isMapStyleDefault) return@setOnClickListener

            isMapStyleDefault = false
            updateMapStyle()
        }

        lightModeView.setOnClickListener {
            if (isMapModeLight) return@setOnClickListener

            isMapModeLight = true
            updateMapStyle()
        }

        darkModeView.setOnClickListener {
            if (!isMapModeLight) return@setOnClickListener

            isMapModeLight = false
            updateMapStyle()
        }

        singleModeCard.setOnClickListener {
            if (!isShowAllVehicle) return@setOnClickListener

            isShowAllVehicle = false
            updateUI()
            customizeOptions()
            changeCurrentVehicle(CurrentVehicleChangeEvent(mPrefRepository.currentVehicle(), mPrefRepository.currentVehicleVrn()))
        }

        allVehicleModeCard.setOnClickListener {
            if (isShowAllVehicle) return@setOnClickListener

            isShowAllVehicle = true
            updateUI()

            showMonitorData()
            mMapDrawer?.focusOnBDLatLng()
        }

        mapDetailsCard.setOnClickListener {
           if (!isContainerViewHidden) return@setOnClickListener

            isContainerViewHidden = false
            customizeOptions()
        }

        vehicleChooser.setOnClickListener {
            showVehicleChooser()

        }

        currentLocationCard.setOnClickListener {
            if (isShowAllVehicle) {
                mMapDrawer?.focusOnBDLatLng()
                return@setOnClickListener
            }

            if (isFocusing) return@setOnClickListener

            if (userLocation == null) {
                mMapDrawer?.focusOnCurrentVehicle((activity as MonitoringActivity).currentVehicleLocation)
                return@setOnClickListener
            }

            isFocusing = true
            mMapDrawer?.focusOnLocation(userLocation ?: LatLng(0.0, 0.0))

            focusHandler.removeCallbacks(focusRunnable)
            focusHandler.postDelayed(focusRunnable, 3000)
        }

        refreshCard.setOnClickListener {
            refreshData()
        }

        context?.let {
            adapter = VehicleCurrentStatusAdapter(it, object : OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int) {
                    val item = monitorViewModel.mutableVehicleCurrentStatusList.value?.get(position)
                            ?: return

                    if (VEHICLE_TYPE == item.id) return
                    VEHICLE_TYPE = item.id

                    selectVehicle()
                }
            })
            adapter.updateVehicleList(ArrayList())
            vehicleStatusList.adapter = adapter
        }

        getCurrentVehicleStatus()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository, object : OnMapClicked {
            override fun onClicked(isClicked: Boolean) {
               isContainerViewHidden = true
               customizeOptions()

            }
        })
        mapFragment.getMapAsync(mMapDrawer)

        mMapDrawer?.setTraffic(true)
        mMapDrawer?.removePolyLine()

        requestPermission()

        setupBottomSheet()

        updateMapStyle()
        updateUI()
    //  customizeOptions()

        observeCurrentTravelledDistance()

        if (isFirstTime && mNetworkAvailabilityChecker.isNetworkAvailable()) {
            showLoading()
        }


        if (!isShowMonitor) {
            showVehicleChooser()
        }

        txtCurrentBstId.text = mPrefRepository.currentVehicle()

        registerNetworkChangeReceiver()


    }

    private fun refreshData() {
        if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
            showLoading()
        }

        mMapDrawer?.clearMap()

        (activity as MonitoringActivity).getVehicleData()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()

        mMapDrawer?.clearMap()

       (activity as MonitoringActivity).getVehicleData()

        appPreference = AppPreferenceImpl(requireContext())

          if (!isShowAllVehicle) {
            mVehicleViewModel.getCurrentVehicle {

                if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.robi.robitrackervts")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast == ""  || TerminalTimeLast == "null" || TerminalTimeLast == null){

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            vehicleModeCard.visibility = View.GONE
                            mapDetailsCard.visibility = View.GONE
                            refreshCard.visibility =View.GONE
                            currentLocationCard.visibility = View.GONE
                            layout_fabIcon.visibility = View.GONE

                            //   Toast.makeText(requireContext(), "NEED MAINTENANCE date null ", Toast.LENGTH_SHORT).show()

                        }else  {

                            var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                            var currentDate = Calendar.getInstance()
                            var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var datetime = Currnetdateformat.format(currentDate.time)


                            var currentDateTime: Date = Currnetdateformat.parse(datetime)

                            var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                            //   hasUpdateIn24Hour  = difference / (60 * 60 * 1000) % 24

                            var hasUpdateIn24Hour: Long = difference / (60 * 60 * 1000)


                            if(hasUpdateIn24Hour!! <= 24){

                                if(!suspendExpiredDialog.shown){

                                    suspendView.visibility = View.VISIBLE
                                    blurView.visibility = View.VISIBLE

                                    vehicleModeCard.visibility = View.GONE
                                    mapDetailsCard.visibility = View.GONE
                                    refreshCard.visibility =View.GONE
                                    currentLocationCard.visibility = View.GONE
                                    layout_fabIcon.visibility = View.GONE

                                    //   Toast.makeText(requireContext(), "SuspendMaintenance :${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                                }

                            }else{

                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                vehicleModeCard.visibility = View.GONE
                                mapDetailsCard.visibility = View.GONE
                                refreshCard.visibility =View.GONE
                                currentLocationCard.visibility = View.GONE
                                layout_fabIcon.visibility = View.GONE

                                //  Toast.makeText(requireContext(), "suspendExpired date 24 hours not data: ${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }else{

                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }

                }else if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.ral.trackmyvehicle")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast.equals("")  || TerminalTimeLast.equals("null") || TerminalTimeLast == null){

                          //   Toast.makeText(requireContext(), "NEED MAINTENANCE date null ", Toast.LENGTH_SHORT).show()
                        }else {

                            var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                            var currentDate = Calendar.getInstance()
                            var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var datetime = Currnetdateformat.format(currentDate.time)


                            var currentDateTime: Date = Currnetdateformat.parse(datetime)

                            var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                            var hasUpdateIn24Hour: Long = difference / (60 * 60 * 1000)

                            if(hasUpdateIn24Hour <= 24){

                                if(!suspendExpiredDialog.shown){

                                    suspendView.visibility = View.VISIBLE
                                    blurView.visibility = View.VISIBLE

                                    vehicleModeCard.visibility = View.GONE
                                    mapDetailsCard.visibility = View.GONE
                                    refreshCard.visibility =View.GONE
                                    currentLocationCard.visibility = View.GONE
                                    layout_fabIcon.visibility = View.GONE

                                    //  Toast.makeText(requireContext(), "SuspendExpire", Toast.LENGTH_SHORT).show()
                                }

                            }else{

                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                vehicleModeCard.visibility = View.GONE
                                mapDetailsCard.visibility = View.GONE
                                refreshCard.visibility =View.GONE
                                currentLocationCard.visibility = View.GONE
                                layout_fabIcon.visibility = View.GONE


                            }
                        }

                    }else{

                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }

                }else{

                    //================ tmv apps Suspended Condition================//

                    if(it?.terminalAssignmentIsSuspended == "1"){
                        if(!suspendDialog.shown){

                            appPreference.SetBstId(AppPreference.bstId,it?.bstId.toString())

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            vehicleModeCard.visibility = View.GONE
                            mapDetailsCard.visibility = View.GONE
                            refreshCard.visibility =View.GONE
                            currentLocationCard.visibility = View.GONE
                            layout_fabIcon.visibility = View.GONE
                          //  suspendDialog.show(parentFragmentManager, "SUSPEND_DIALOG")
                        }
                    }else{

                        //  customizeOptions()
                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }
                }

            }
        }

/*  if (!isShowAllVehicle) {
            mVehicleViewModel.getCurrentVehicle {

                if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.robi.robitrackervts")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast == ""  || TerminalTimeLast == null){

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            vehicleModeCard.visibility = View.GONE
                            mapDetailsCard.visibility = View.GONE
                            refreshCard.visibility =View.GONE
                            currentLocationCard.visibility = View.GONE
                            layout_fabIcon.visibility = View.GONE

                            suspendMaintenanceDialog.show(parentFragmentManager, "SuspendMaintenance")

                            //   Toast.makeText(requireContext(), "NEED MAINTENANCE date null ", Toast.LENGTH_SHORT).show()

                        }else  {

                            var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                            var currentDate = Calendar.getInstance()
                            var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var datetime = Currnetdateformat.format(currentDate.time)


                            var currentDateTime: Date = Currnetdateformat.parse(datetime)

                            var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                            //   hasUpdateIn24Hour  = difference / (60 * 60 * 1000) % 24

                            var hasUpdateIn24Hour: Long = difference / (60 * 60 * 1000)
                            //long diffHours = diff / (60 * 60 * 1000);


                            if(hasUpdateIn24Hour!! <= 24){

                                if(!suspendExpiredDialog.shown){

                                    suspendView.visibility = View.VISIBLE
                                    blurView.visibility = View.VISIBLE

                                    vehicleModeCard.visibility = View.GONE
                                    mapDetailsCard.visibility = View.GONE
                                    refreshCard.visibility =View.GONE
                                    currentLocationCard.visibility = View.GONE
                                    layout_fabIcon.visibility = View.GONE

                                    suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                                    //   Toast.makeText(requireContext(), "SuspendMaintenance :${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                                }

                            }else{

                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                vehicleModeCard.visibility = View.GONE
                                mapDetailsCard.visibility = View.GONE
                                refreshCard.visibility =View.GONE
                                currentLocationCard.visibility = View.GONE
                                layout_fabIcon.visibility = View.GONE

                                suspendExpiredDialog.show(requireFragmentManager(), "SuspendExpire")

                                //  Toast.makeText(requireContext(), "suspendExpired date 24 hours not data: ${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }else{

                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }

                }else if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.ral.trackmyvehicle")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast.equals("")  || TerminalTimeLast.equals("null")){

                            suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                            //   Toast.makeText(requireContext(), "NEED MAINTENANCE date null ", Toast.LENGTH_SHORT).show()
                        }else {

                            var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                            var currentDate = Calendar.getInstance()
                            var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var datetime = Currnetdateformat.format(currentDate.time)


                            var currentDateTime: Date = Currnetdateformat.parse(datetime)

                            var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                            var hasUpdateIn24Hour: Long = difference / (60 * 60 * 1000)

                            if(hasUpdateIn24Hour <= 24){

                                if(!suspendExpiredDialog.shown){

                                    suspendView.visibility = View.VISIBLE
                                    blurView.visibility = View.VISIBLE

                                    vehicleModeCard.visibility = View.GONE
                                    mapDetailsCard.visibility = View.GONE
                                    refreshCard.visibility =View.GONE
                                    currentLocationCard.visibility = View.GONE
                                    layout_fabIcon.visibility = View.GONE



                                    suspendExpiredDialog.show(requireFragmentManager(), "SuspendExpire")

                                    //  Toast.makeText(requireContext(), "SuspendExpire", Toast.LENGTH_SHORT).show()
                                }

                            }else{

                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                vehicleModeCard.visibility = View.GONE
                                mapDetailsCard.visibility = View.GONE
                                refreshCard.visibility =View.GONE
                                currentLocationCard.visibility = View.GONE
                                layout_fabIcon.visibility = View.GONE

                                suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")
                            }
                        }

                    }else{

                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }

                }else{

                    //================ tmv apps Suspended Condition================//

                    if(it?.terminalAssignmentIsSuspended == "1"){
                        if(!suspendDialog.shown){

                            appPreference.SetBstId(AppPreference.bstId,it?.bstId.toString())

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            vehicleModeCard.visibility = View.GONE
                            mapDetailsCard.visibility = View.GONE
                            refreshCard.visibility =View.GONE
                            currentLocationCard.visibility = View.GONE
                            layout_fabIcon.visibility = View.GONE
                            suspendDialog.show(parentFragmentManager, "SUSPEND_DIALOG")
                        }
                    }else{

                        //  customizeOptions()
                        val location = LatLng((it?.terminalDataLatitudeLast
                            ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                        mMapDrawer?.focusOnLocation(location)

                    }
                }

            }
        } */


    }


    fun showVehicleChooser() {

        val dialogFrag = BottomNavFragment.newInstance()
        activity?.supportFragmentManager?.let { dialogFrag.show(it, dialogFrag.tag) }
    }

    private fun selectVehicle() {
        mMapDrawer?.focusOnBDLatLng()
        adapter.setSelectedView(VEHICLE_TYPE)
        onVehicleGroupSelected()
    }

    private fun onVehicleGroupSelected() {
        when (VEHICLE_TYPE) {
            ALL_VEHICLES_TYPE -> {
                mMapDrawer?.setVehicleList(allVehicles, true)

            }
            MOVING_VEHICLES_TYPE -> {
                mMapDrawer?.setVehicleList(movingVehicles, true)

            }
            IDLE_VEHICLES_TYPE -> {
                mMapDrawer?.setVehicleList(idleVehicles, true)

            }
            ENGINE_OFF_VEHICLES_TYPE -> {
                mMapDrawer?.setVehicleList(engineOffVehicles, true)

            }
            else -> {
                mMapDrawer?.setVehicleList(offlineVehicles, true)
            }
        }
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehaviorGoogleMapsLike.from<View>(bottom_sheet)
        behavior.addBottomSheetCallback(object :
                BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED -> Log.e("bottomsheet-",
                            "STATE_COLLAPSED")
                    BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING -> Log.e("bottomsheet-",
                            "STATE_DRAGGING")
                    BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED -> {
                        Log.e("bottomsheet-", "STATE_EXPANDED")
                    }
                    BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT -> Log.e("bottomsheet-",
                            "STATE_ANCHOR_POINT")
                    BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN -> Log.e("bottomsheet-",
                            "STATE_HIDDEN")
                    else -> Log.e("bottomsheet-", "STATE_SETTLING")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED

        listReportButtons.adapter = ReportButtonAdapter(this)
        listReportButtons.layoutManager = GridLayoutManager(context, 2)
    }

    fun updateVehicleList(terminalList: ArrayList<Terminal>) {
        dismissDialog()

        if (terminalList.size == 0)
            return

        allVehicles.clear()
        offlineVehicles.clear()
        idleVehicles.clear()
        movingVehicles.clear()
        engineOffVehicles.clear()

        for (terminal in terminalList) {
            if (terminal.terminalDataLatitudeLast?.toDoubleOrNull() != null
                    && terminal.terminalDataLongitudeLast?.toDoubleOrNull() != null) {
                when (getVehicleState(terminal)) {
                    VEHICLE_ENGINE_OFF -> {
                        engineOffVehicles.add(terminal)
                    }
                    VEHICLE_OFFLINE -> {
                        offlineVehicles.add(terminal)
                    }
                    VEHICLE_IDLE -> {
                        idleVehicles.add(terminal)
                    }
                    VEHICLE_MOVING -> {
                        movingVehicles.add(terminal)
                    }
                }

                allVehicles.add(terminal)
            }

            if (mPrefRepository.currentVehicle() == terminal.bstId) {

                //Catch terminal suspend
                val data = convertVehicleStatus(terminal)
                setupDrawerData(data)
                if(terminal.terminalAssignmentIsSuspended == "1"){
                    if(!suspendDialog.shown){
                        //suspendDialog.show(parentFragmentManager, "SUSPEND_DIALOG")
                    }
                }else{

                    lastLocation = (activity as MonitoringActivity).currentVehicleLocation
                    (activity as MonitoringActivity).currentVehicleLocation = data

                    if (isFirstTime) {
                        isFirstTime = false
                        mMapDrawer?.focusOnCurrentVehicle((activity as MonitoringActivity).currentVehicleLocation)
                    }
                }
            }
        }

        if (isShowAllVehicle) {
            showMonitorData()

        } else {
            showSingleVehicleData()
        }
    }

    private fun updateUI() {
        try {
            context.let {
                allVehicleModeCard.setCardBackgroundColor(ContextCompat.getColor(it!!, if (isShowAllVehicle) R.color.colorPrimary else R.color.white))
                singleModeCard.setCardBackgroundColor(ContextCompat.getColor(it, if (!isShowAllVehicle) R.color.colorPrimary else R.color.white))

                allVehicleModeImage.setColorFilter(ContextCompat.getColor(it, if (isShowAllVehicle) R.color.blackTmvWhiteRvtRal else R.color.black))
                singleModeImage.setColorFilter(ContextCompat.getColor(it, if (!isShowAllVehicle) R.color.blackTmvWhiteRvtRal else R.color.black))

                vehicleStatusList.visibility = if (isShowAllVehicle) View.VISIBLE else View.GONE

                bottom_sheet.visibility = if (!isShowAllVehicle) View.VISIBLE else View.GONE
                layout_fabIcon.visibility = if (!isShowAllVehicle) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e("TAG", "updateUI: Something went wrong")
        }
    }

    fun customizeOptions() {

        mapModeContainer.visibility = if (!isContainerViewHidden) View.VISIBLE else View.GONE

        mapDetailsCard.visibility = if (isContainerViewHidden) View.VISIBLE else View.GONE
        vehicleModeCard.visibility = if (isContainerViewHidden && isShowMonitor) View.VISIBLE else View.GONE
        currentLocationCard.visibility = if (isContainerViewHidden) View.VISIBLE else View.GONE
        refreshCard.visibility = if (isContainerViewHidden) View.VISIBLE else View.GONE
        vehicleChooser.visibility = if (isContainerViewHidden && !isShowMonitor) View.VISIBLE else View.GONE
    }

    private fun showMonitorData() {
        mMapDrawer?.clearMap()
        monitorViewModel.setCurrentVehicleStatus(allVehicles, offlineVehicles, idleVehicles, movingVehicles, engineOffVehicles)

        onVehicleGroupSelected()
    }

    private fun showSingleVehicleData() {

        mMapDrawer?.setVehicleList(allVehicles, false)
        mMapDrawer?.placeMarkerAtCurrentPosition((activity as MonitoringActivity).currentVehicleLocation)

    }

    private fun getCurrentVehicleStatus() {
        monitorViewModel.getVehicleCurrentStatusList().observe(viewLifecycleOwner, object : Observer<List<VehicleCurrentStatusModel>> {
            override fun onChanged(data: List<VehicleCurrentStatusModel>?) {
                adapter.updateVehicleList(data as ArrayList<VehicleCurrentStatusModel>)
                adapter.notifyDataSetChanged()


            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(ARG_PARAM1, param1)
//                        putString(ARG_PARAM2, param2)
//                    }
        }
    }

    private fun showLoading() {
        dismissDialog()
        mDialog = DialogHelper.getLoadingDailog(requireActivity(), getString(R.string.msg_hold_on),
                getString(R.string.msg_loading))
                ?.show()
    }

    private fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }

    fun updateCurrentVehicleStatus(data: VehicleStatus?) {
        if (mCurrentDistanceTravelLiveData == null || mCurrentDistanceTravelLiveData?.hasObservers() == false) {
            observeCurrentTravelledDistance()
        }

        if (data?.bstid != mVehicleViewModel.mPrefRepository.currentVehicle()) {
            return
        }

        vehicleStatusData = data

        setupDrawerData(data)

        btnPayNow.visibility = View.GONE
        if (allVehicles.isEmpty()) {
            containerPayment.visibility = View.GONE
        } else {
            val terminal = allVehicles.firstOrNull { it.bstId == mVehicleViewModel.mPrefRepository.currentVehicle() }
            containerPayment.visibility = if (terminal?.isSuspended() == true) View.VISIBLE else View.GONE
           txtAlert.text = "Service suspended! Please pay your bills."
        }
    }

    private fun setupDrawerData(data: VehicleStatus?) {
        txtCurrentBstId.text = data?.bstid
        txtVehicleAliasName.text = data?.bstid

        txtVehicleId.text = data?.vrn

        if(data?.location?.place.toString().equals("--")){

            txtVehicleLastLocation.text = "---"

        }else{

            txtVehicleLastLocation.text = "Nearby of "+data?.location?.place

        }


        data?.updatedAt?.let {
            txtVehicleLastUpdatedAt.text = if (data.updatedAt.isNullOrEmpty()) "" else getRelativeTimeFromNow(
                    DateTime.parse(it, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")))
        }

        txtEngineStatus.text = data?.engineStatus

        val lastUpdated = try {
            DateTime.parse(data?.updatedAt, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (ex: Exception) {
            null
        }
        imgEngineStatus.setImageDrawable(
                if ((lastUpdated?.isBefore(DateTime.now().minusDays(1)) == true) || data?.engineStatus?.contains("--") == true) {
                    context?.let { ContextCompat.getDrawable(it, R.drawable.engine_disable) }
                } else if (data?.engineStatus == "ON" && (data.speed?.toFloatOrNull()
                                ?: 0.0) == 0.0) context?.let {
                    ContextCompat.getDrawable(it,
                            R.drawable.engine_idle)
                }
                else if (data?.engineStatus == "ON") context?.let {
                    ContextCompat.getDrawable(it,
                            R.drawable.engine_on)
                }
                else context?.let { ContextCompat.getDrawable(it, R.drawable.engine_off) }
        )
        txtVehicleSpeed.text = (data?.speed ?: "--") + "KM/H"
    }

    fun setProgressVisibility(visibility: Int) {
        if (visibility != View.VISIBLE) {
            dismissDialog()
        }
        progressBar?.visibility = visibility
    }

    private fun observeCurrentTravelledDistance() {
        try {
            txtTravelledDistance.text = "--"
            progressTravelledDistance.visibility = View.GONE

            val terminalId = mPrefRepository.currentVehicleTerminalId

            mCurrentDistanceTravelLiveData?.removeObserver(mCurrentDistanceTravelObserver)
            mCurrentDistanceTravelLiveData = mVehicleViewModel.getTodaysTravelledDistance(terminalId)
            mCurrentDistanceTravelLiveData?.observe(viewLifecycleOwner, mCurrentDistanceTravelObserver)

        } catch (e: Exception) {
            Log.e("TAG", "observeCurrentTravelledDistance: ${e.message}")
        }
    }

    fun fetchCurrentTravelledDistance() {
        val currentVehicle = mPrefRepository.currentVehicle()
        val currentTerminal = allVehicles.firstOrNull { it.bstId == currentVehicle }
        currentTerminal?.terminalID?.toString()?.let {
            mVehicleViewModel.fetchTodaysTravelledDistance(it)
        }
    }

    val br = NetworkChangeReceiver()
    private fun registerNetworkChangeReceiver() {
        val filter = IntentFilter().apply {
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
            addAction("android.net.wifi.WIFI_STATE_CHANGED")
        }
        activity?.registerReceiver(br, filter)
    }

    private fun unregisterReceiver() {
        activity?.unregisterReceiver(br)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetworkStatusChange(event: NetworkConnectivityEvent) {
        activity?.runOnUiThread {
            txtNoInternetText.visibility = if (event.connected) View.GONE else View.VISIBLE
        }
    }

    private fun updateMapStyle() {
        context.let {
            defaultMapText.setTextColor(ContextCompat.getColor(it!!, if (isMapStyleDefault) R.color.colorPrimary else R.color.black))
            satelliteMapText.setTextColor(ContextCompat.getColor(it, if (!isMapStyleDefault) R.color.colorPrimary else R.color.black))

            defaultMapImage.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(it, if (isMapStyleDefault) R.color.colorPrimary else R.color.white))
            satelliteMapImage.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(it, if (!isMapStyleDefault) R.color.colorPrimary else R.color.white))

            lightModeText.setTextColor(ContextCompat.getColor(it, if (isMapModeLight) R.color.colorPrimary else R.color.black))
            darkModeText.setTextColor(ContextCompat.getColor(it, if (!isMapModeLight) R.color.colorPrimary else R.color.black))

            lightModeCard.setCardBackgroundColor(ContextCompat.getColor(it, if (isMapModeLight) R.color.colorPrimary else R.color.nightModeColorBackground))
            darkModeCard.setCardBackgroundColor(ContextCompat.getColor(it, if (!isMapModeLight) R.color.colorPrimary else R.color.nightModeColorBackground))

            mMapDrawer?.setMapMode(isMapStyleDefault, isMapModeLight)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        isShowAllVehicle = false
        updateUI()
       //customizeOptions()
        changeCurrentVehicle(event)
    }

    override fun onDestroy() {
        super.onDestroy()

        focusHandler.removeCallbacks(focusRunnable)

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        unregisterReceiver()
    }

    private fun changeCurrentVehicle(event: CurrentVehicleChangeEvent) {
        activity?.runOnUiThread {
            getVehicleFromList(event.bstId)
            isShowAllVehicle = false
          showSingleVehicle()
           observeCurrentTravelledDistance()
        }
    }

    private fun getVehicleFromList(bstId: String) {
        for (item in allVehicles) {
            if (item.bstId == bstId) {
                val data = convertVehicleStatus(item)
                lastLocation = (activity as MonitoringActivity).currentVehicleLocation
                (activity as MonitoringActivity).currentVehicleLocation = data

                break
            }
        }
    }

    private fun showSingleVehicle() {
        mMapDrawer?.removePolyLine()
        mMapDrawer?.clearMap()
        mMapDrawer?.mClusterManager?.clearItems()

  //  mVehicleViewModel.changeCurrentVehicle(vehicleStatusData?.bstid ?: "", vehicleStatusData?.vrn ?: "", vehicleStatusData?.bid.toString(), "${vehicleStatusData?.location?.latitude},${vehicleStatusData?.location?.longitude}")

  //  mMapDrawer?.setupMarker()
        //  Toast.makeText(requireContext(),"showSingleVehicle",Toast.LENGTH_SHORT).show()

        mMapDrawer?.placeMarkerAtCurrentPosition((activity as MonitoringActivity).currentVehicleLocation)

        mVehicleViewModel.getCurrentVehicle {

            val location = LatLng((it?.terminalDataLatitudeLast
                    ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
            mMapDrawer?.focusOnLocation(location)
        }
    }

    private fun requestPermission() {
        (activity as MonitoringActivity).let {
            ActivityCompat.requestPermissions(
                    it,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION
            )
        }
    }

    fun requestToEnableGPS() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val result: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response: LocationSettingsResponse? =
                        task.getResult(ApiException::class.java)

                context?.let { checkLocationPermission(it) }

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                        activity?.let {
                            resolvable.startResolutionForResult(
                                    it,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    val callback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) return

            userLocation = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
            )
        }
    }

    fun checkLocationPermission(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }

        mMapDrawer?.mMap?.isMyLocationEnabled = true

        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = LOCATION_REFRESH_TIME.toLong()
        mLocationRequest.fastestInterval = LOCATION_REFRESH_TIME.toLong()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                callback,
                Looper.getMainLooper()
        )
    }

    override fun onClick(model: ReportButtonModel) {
        context.let {
            val contextData: Context = it!!
            when (model.slug) {
                "Hourly Report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_HOURLY_DISTANCE_REPORT)
                }
                "Engine report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_ENGINE_ON_REPORT)
                }
                "Vehicle Route Analytics".toLowerCase().replace(" ", "-") -> {
                    startActivity(Intent(contextData, VehicleRouteAnalyticsActivity::class.java))
                }
                "Distance Report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_DISTANCE_REPORT)
                }
                "Monthly Report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_MONTHLY_DISTANCE_REPORT)
                }
                "Pay Bill".toLowerCase().replace(" ", "-") -> {
                    BillingActivity.intent(contextData)
                }
//                "Notifications".toLowerCase().replace(" ", "-") -> {
//                    switchToNotificationVisibilityHandle()
//
//                    var notificationFragment: NotificationFragment = NotificationFragment()
//                    supportFragmentManager.beginTransaction().add(R.id.fragment_container, notificationFragment).commit()
//                    //FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
//                }
                "Subscription".toLowerCase().replace(" ", "-") -> {
                }
                "Location Report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_LOCATION_REPORT)
                }
                "Disarm Engine".toLowerCase().replace(" ", "-") -> {
                    SecureModeActivity.intent(contextData)
                }
                "Speed report".toLowerCase().replace(" ", "-") -> {
                    FragmentActivity.startActivity(contextData, FragmentActivity.TAG_SPEED_REPORT)
                }
            }
        }

    }
}