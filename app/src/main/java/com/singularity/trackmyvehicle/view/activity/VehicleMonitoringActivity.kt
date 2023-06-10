package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.gaurav.gesto.OnGestureListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.admin.AdminNavigation
import com.singularity.trackmyvehicle.admin.IAdminNavigation
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleCurrentStatusModel
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.model.event.NetworkConnectivityEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.repository.interfaces.OnItemClickListener
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.view.adapter.*
import com.singularity.trackmyvehicle.view.customview.behaviour.BottomSheetBehaviorGoogleMapsLike
import com.singularity.trackmyvehicle.view.decoration.HorizontalMarginItemDecoration
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.view.fragment.NotificationFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewSegment.CurrentStatusTicker
import com.singularity.trackmyvehicle.view.viewSegment.HasCurrentVehicleStatus
import com.singularity.trackmyvehicle.view.viewSegment.UsesMapWithDrawer
import com.singularity.trackmyvehicle.viewmodel.*
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home_2.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_content_toolbar.*
import kotlinx.android.synthetic.main.layout_current_location.*
import kotlinx.android.synthetic.main.layout_current_location.cardView_current_location
import kotlinx.android.synthetic.main.layout_current_location.fabCurrentLocation
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import kotlinx.android.synthetic.main.layout_device_id_toolbar.*
import kotlinx.android.synthetic.main.layout_device_id_toolbar.imgChangeVehicle
import kotlinx.android.synthetic.main.layout_device_id_toolbar.txtCurrentBstId
import kotlinx.android.synthetic.main.layout_home_content.*
import kotlinx.android.synthetic.main.layout_home_content.bottom_sheet
import kotlinx.android.synthetic.main.layout_home_content.fabDirection
import kotlinx.android.synthetic.main.layout_home_content.layout_fabIcon
import kotlinx.android.synthetic.main.layout_home_content.virtualWatchman
import kotlinx.android.synthetic.main.layout_map_details.*
import kotlinx.android.synthetic.main.layout_vehicle_status_item.*
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.*
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.cardView_allVehicleMode
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.cardView_singleVehicleMode
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.cardView_vehicleMode
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.imageView_all_vehicle_mode
import kotlinx.android.synthetic.main.layout_vehicle_switch_mode.imageView_individual_vehicle_mode
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs


class VehicleMonitoringActivity : AppCompatActivity(),
        HasCurrentVehicleStatus,
        UsesMapWithDrawer,
        CurrentStatusTicker,
        OnItemClickCallback<ReportButtonModel>,
        AdminNavigation by IAdminNavigation(){

    private var mCurrentDistanceTravelLiveData: LiveData<TerminalAggregatedData>? = null
    private var menuItemId: Int = -1
    private var vehicles = ArrayList<Terminal>()
    private var movingVehicles = ArrayList<Terminal>()
    private var offlineVehicles = ArrayList<Terminal>()
    private var idleVehicles = ArrayList<Terminal>()
    private var engineOffVehicles = ArrayList<Terminal>()

    var VEHICLE_TYPE = ALL_VEHICLES_TYPE

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        menuItemId = item.itemId
        when (item.itemId) {

            R.id.navigation_home -> {
                isSwitchFragmentAnlytics = false
                isSwitchFragmentNotification = false

                if (isVehicleModeAllVehicle == false) {
                    bottom_sheet.visibility = View.VISIBLE
                    layout_fabIcon.visibility = View.VISIBLE
                } else {
                    bottom_sheet.visibility = View.GONE
                    layout_fabIcon.visibility = View.GONE
                }

//                cardView_map_details.visibility = View.VISIBLE
                cardView_vehicleMode.visibility = View.VISIBLE
                layout_switchVehicleMode.visibility = View.VISIBLE
                cardView_current_location.visibility = View.VISIBLE
                layout_deviceId.visibility = View.VISIBLE

                layout_map_details.visibility = View.VISIBLE
                fragment_container.visibility = View.GONE

                fabAnalytics.visibility = View.VISIBLE
                textView_analytics.visibility = View.GONE
                textView_liveMap.visibility = View.VISIBLE
                fabMonitor.visibility = View.GONE
                textView_vehicles.visibility = View.GONE
                fabVehicles.visibility = View.VISIBLE
                recyclerView_vehicleStatus.visibility = View.VISIBLE
                layout_notifications.visibility = View.VISIBLE
                textView_notifications.visibility = View.GONE

                startActivity(Intent(this, VehicleMonitoringActivity::class.java))
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_route -> {
                //var intent = Intent(this, VehicleRouteActivity::class.java)
                //startActivity(intent)
                //startActivity(Intent(this, HomeActivity::class.java))
                //startActivity(Intent(this, VehicleRouteAnalyticsActivity::class.java))
                var intent = Intent(this, VehicleRouteAnalyticsActivity::class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_account -> {
                //FragmentActivity.startActivity(this, FragmentActivity.TAG_ACCOUNT)
                //startActivity(Intent(this, AccountActivity::class.java))
                var intent = Intent(this, AccountActivity::class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_expense -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_EXPENSE_LIST)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_daily_distance -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_DISTANCE_REPORT)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_daily_engine -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_ENGINE_ON_REPORT)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notification -> {

                switchToNotificationVisibilityHandle()

                //FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
                var notificationFragment: NotificationFragment = NotificationFragment()
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, notificationFragment).commit()
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_pay_bill -> {
                BillingActivity.intent(this)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_support -> {
                startActivity(Intent(this, HelpAndSupportActivity::class.java))
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_hourly_distance -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_HOURLY_DISTANCE_REPORT)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_location_report -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_LOCATION_REPORT)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_monthly_report -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_MONTHLY_DISTANCE_REPORT)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_trip_report -> {
                var intent = Intent(this, TripReportActivity::class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_logout -> {
                mAnalyticsViewModel.logOut()
                mProfileViewModel.logout {
                    VehicleTrackApplication.app?.startLogoutProcedure({
                        if (VehicleTrackApplication.app?.isCurrentlyLoginActivity == false) {
                            val intent = Intent(this, SplashScreenActivity::class.java)
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra(SplashScreenActivity.EXTRA_REMOVE_DATA, true)
                            this.startActivity(intent)
                        }
                    }, true)
                }

            }
            else -> {
                if (shouldGotoSwitchUserScreen(item))
                    gotoSwitchUserScreen(this@VehicleMonitoringActivity)
            }
        }
        false
    }

    private fun closeDrawer() {
        container.closeDrawer(GravityCompat.START)
    }

    @Inject
    override lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mFCMRepository: FCMRepository
    @Inject
    lateinit var mNetworkAvailabilityChecker: NetworkAvailabilityChecker
    @Inject
    lateinit var userSource: UserSource

    @Inject
    override lateinit var mVehicleRepository: VehicleRepository
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    @Inject
    lateinit var mProfileViewModel: ProfileViewModel
    @Inject
    lateinit var mAnalyticsViewModel: AnalyticsViewModel
    @Inject
    lateinit var mNotificationViewModel: NotificationViewModel

    @Inject
    lateinit var appPreference: AppPreference

    override var mCurrentVehicleStatusLiveData: LiveData<Resource<VehicleStatus>>? = null
    override var mCurrentStatusHandler: Handler = Handler()
    override var mCurrentStatusRunnable: Runnable = currentStatusRunnable()
    private var mCurrentVehicleStatus: VehicleStatus? = null

    private var mDialog: MaterialDialog? = null

    var terminalId : String? = ""

    var userName : String = ""

    var updateCurrentVehicleCount = 0

    lateinit var adapter : VehicleCurrentStatusAdapter

    private lateinit var vehicleMonitoringViewModel : VehicleMonitoringViewModel

    var vehicleUpdateType : Int = VEHICLE_UPDATE_TYPE

    var selectedVehicleLatLng : LatLng? = LatLng(0.00, 0.00)

    var isVehicleModeAllVehicle = false

    var isSwitchFragmentAnlytics = false

    var isSwitchFragmentNotification = false

    var vehicleStatusData: VehicleStatus? = null

    val vehicleStatus = VehicleStatus()

    private var fusedLocationClient: FusedLocationProviderClient? = null

//    override fun getActivity(): Activity {
//        return this
//    }

    override fun updateCurrentVehicleStatus(data: VehicleStatus?) {
        updateCurrentVehicleCount++
        vehicleStatusData = data

        Log.d("ukgt", "Entered update current vehicle status ${Gson().toJson(data)}")

        if (mCurrentDistanceTravelLiveData == null || mCurrentDistanceTravelLiveData?.hasObservers() == false) {
            observeCurrentTravelledDistance()
        }

        if (data?.bstid != mVehicleViewModel.mPrefRepository.currentVehicle()) {
            return
        }

        if(data != null){
            dismissDialog()
        }

        if(isVehicleModeAllVehicle == false){
            Log.d("hfguj", "isVehicleModeAllVehicle $isVehicleModeAllVehicle 3")
            mMapDrawer?.placeMarkerAtCurrentPosition(data, isFirstTime = mCurrentVehicleStatus == null)

            if (mCurrentVehicleStatus?.bstid != data?.bstid) {
                mMapDrawer?.focusOnCurrentStatus()
            }
        }

        /*if (mCurrentVehicleStatus?.bstid != data?.bstid) {
            Log.d("kjb","${Gson().toJson(data)}")
                mMapDrawer?.focusOnCurrentStatusFromVehicleMonitoring(data)

        }*/

        if(updateCurrentVehicleCount == 1){
            mMapDrawer?.focusOnCurrentStatus()
            if (data?.location?.latitude != null && data?.location?.latitude != null) {
                return try {
                    selectedVehicleLatLng = LatLng(data?.location?.latitude?.toDouble()
                            ?: 0.00, data?.location?.longitude?.toDouble() ?: 0.00)

                } catch (e: java.lang.Exception) {
                    
                }
            }
        }

        mCurrentVehicleStatus = data

        // TODO: Update texts
        txtCurrentBstId.text = data?.bstid
        txtVehicleAliasName.text = data?.bstid

        txtVehicleId.text = data?.vrn
        txtVehicleLastLocation.text = data?.location?.place
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
                    ContextCompat.getDrawable(this, R.drawable.engine_disable)
                } else if (data?.engineStatus == "ON" && (data.speed?.toFloatOrNull()
                                ?: 0.0) == 0.0) ContextCompat.getDrawable(this,
                        R.drawable.engine_idle)
                else if (data?.engineStatus == "ON") ContextCompat.getDrawable(this,
                        R.drawable.engine_on)
                else ContextCompat.getDrawable(this, R.drawable.engine_off)
        )
        txtVehicleSpeed.text = (data?.speed ?: "--") + "KM/H"

        btnPayNow.visibility = View.GONE
        if (vehicles.isEmpty()) {
            containerPayment.visibility = View.GONE
        } else {
            val terminal = vehicles.firstOrNull { it.bstId == mVehicleViewModel.mPrefRepository.currentVehicle() }
            containerPayment.visibility = if (terminal?.isSuspended() == true) View.VISIBLE else View.GONE
            txtAlert.text = "Service suspended! Please pay your bills."
        }

    }

    override fun setProgressVisibility(visibility: Int) {
        progressBar.visibility = visibility
    }

    override var mMapDrawer: MapDrawer? = null

    override fun fetchCurrentLocation() {
        mCurrentVehicleStatusLiveData = mVehicleViewModel.getCurrentVehicleStatus()

        mCurrentVehicleStatusLiveData?.observe(this, currentVehicleStatusObserver())
    }

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

    private fun observeCurrentTravelledDistance() {
        txtTravelledDistance.text = "--"
        progressTravelledDistance.visibility = View.GONE

        val currentVehicle = mPrefRepository.currentVehicle()
        val currentTermnal = vehicles.firstOrNull { it.bstId == currentVehicle }
        if (currentTermnal != null) {
            mCurrentDistanceTravelLiveData?.removeObserver(mCurrentDistanceTravelObserver)
            mCurrentDistanceTravelLiveData = mVehicleViewModel.getTodaysTravelledDistance(
                    currentTermnal.terminalID.toString())
            mCurrentDistanceTravelLiveData?.observe(this, mCurrentDistanceTravelObserver)
        }
    }

    override fun fetchCurrentTravelledDistance() {
        val currentVehicle = mPrefRepository.currentVehicle()
        val currentTermnal = vehicles.firstOrNull { it.bstId == currentVehicle }
        currentTermnal?.terminalID?.toString()?.let {
            mVehicleViewModel.fetchTodaysTravelledDistance(it)
        }
    }

    private val offlineDataObserver = object : Observer<List<Terminal>> {
        override fun onChanged(data: List<Terminal>?) {
            if ((data ?: ArrayList()).isEmpty()) return

            updateVehicleList(data as ArrayList<Terminal>)
        }
    }

    override fun onResume() {
        super.onResume()

        mMapDrawer?.focusOnCurrentStatus()

        mVehicleViewModel.getVehicles()

        val headerView = navigationDrawerView.getHeaderView(0)
        mProfileViewModel.fetchOrGetProfileInformation().observe(this, Observer {
            headerView.findViewById<TextView>(R.id.txtNvName).text = it.data?.name
                    ?: "Bondstein Technologies Ltd."
            headerView.findViewById<TextView>(R.id.txtNvEmail).text = it.data?.email
                    ?: Constants.VISIT_URL
        })
        val shouldShowSwitchUserMenu = (mPrefRepository.user?.userGroupIdentifier?.toLowerCase() == "ADMINISTRATOR".toLowerCase()
                && mPrefRepository.userSource == UserSource.VERSION_3.identifier)
        if(!shouldShowSwitchUserMenu) {
            hideSwitchUserMenuItem(navigationDrawerView.menu)
        }

        observerNotifications()

        txtNoInternetText.visibility = if (mNetworkAvailabilityChecker.isNetworkAvailable()) View.GONE
        else View.VISIBLE

    }

    override fun onDestroy() {
        super.onDestroy()

        enteredSingleVehicle = 0
        enteredInChangeCurrentVehicle = 0
        enteredupdateVehicleList = 0

        stopTicker()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startTicker()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        setContentView(R.layout.activity_vehicle_monitoring)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        vehicleMonitoringViewModel = ViewModelProvider(this).get(VehicleMonitoringViewModel::class.java)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = " "
        txtCurrentBstId.setOnTouchListener(object : OnGestureListener(this) {
            override fun onSwipeBottom() {
                selectNextVehicle()

            }

            override fun onSwipeTop() {
                selectPreviousVehicle()
            }

            override fun onClick() {
                val dialogFrag = BottomNavFragment.newInstance()
                dialogFrag.show(supportFragmentManager, dialogFrag.tag)
            }
        })

        vehicleUpdateType = this.intent.extras?.getInt("afterLoginVehicleUpdateType") ?: VEHICLE_UPDATE_TYPE
        updateCurrentVehicleCount = 0
        setUpMap(this, supportFragmentManager)
        hideNecessaryNavigationItems()
        hideNotificationFabIfRequired()

        navigationDrawerView?.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val headerView = navigationDrawerView.getHeaderView(0)
        headerView.setOnClickListener {
            FragmentActivity.startActivity(this, FragmentActivity.TAG_ACCOUNT)
            closeDrawer()
        }
        mProfileViewModel.fetchOrGetProfileInformation().observe(this, Observer {
            headerView.findViewById<TextView>(R.id.txtNvName).text = it.data?.name
                    ?: "Bondstein Technologies Ltd."
            headerView.findViewById<TextView>(R.id.txtNvEmail).text = it.data?.email
                    ?: Constants.VISIT_URL
            userName = it.data?.name.toString()
        })

        imgDrawerMenu.setOnClickListener {
            container.openDrawer(GravityCompat.START)
        }
        fabDirection.setOnClickListener {
            //val currentVehicleStatus = mMapDrawer?.currentVehicleStatus()
            val currentVehicleStatus = selectedVehicleLatLng
            //val lat = currentVehicleStatus?.location?.latitude?.toDoubleOrNull() ?: 0.0
            val lat = currentVehicleStatus?.latitude ?: 0.0
            //val lon = currentVehicleStatus?.location?.longitude?.toDoubleOrNull() ?: 0.0
            val lon = currentVehicleStatus?.longitude ?: 0.0
            if (currentVehicleStatus != null && lat != 0.0 && lon != 0.0) {
                val url = "https://www.google.com/maps/dir/?api=1&origin=$lat,$lon&travelmode=driving"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toasty.error(this, "Location not available").show()
            }
        }

        virtualWatchman.setOnClickListener {

            if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
                if (terminalId == null || terminalId == "" || terminalId == "null"){
                    //return@setOnClickListener
                    Toast.makeText(this@VehicleMonitoringActivity, "Terminal id not found, Please try again", Toast.LENGTH_SHORT).show()

                } else {
                    val intent = Intent(this@VehicleMonitoringActivity, VirtualWatchmanSet::class.java)
                    intent.putExtra("Terminal_ID", terminalId)
                    intent.putExtra("Selected_Vehicle_Latitude", selectedVehicleLatLng?.latitude)
                    intent.putExtra("Selected_Vehicle_Longitude", selectedVehicleLatLng?.longitude)
                    startActivity(intent)
                }

            } else {
                Toast.makeText(this, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()
            }

        }

        fabNotification.setOnClickListener {
            switchToNotificationVisibilityHandle()

            //FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
            var notificationFragment : NotificationFragment = NotificationFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, notificationFragment).commit()

        }

        fetchCurrentLocation()

        initConstrainViews()

        mMapDrawer?.setTraffic(true)
        mMapDrawer?.removePolyLine()

        imgChangeVehicle.setOnClickListener {
            bottomNavShow()
        }

        observeCurrentTravelledDistance()
        fetchCurrentTravelledDistance()

        observerNotifications()

        when (userSource) {
            UserSource.VERSION_2 -> {
                containerTravelDistance.visibility = View.GONE
                dividerTravelEngine.visibility = View.GONE
                containerStatus.weightSum = 2.1f
            }
            else -> {
            }
        }

//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
//            mFCMRepository.postToken(it.token)
//        }

        if (mFCMRepository.shouldSendFCMToken() && appPreference.getBoolean(AppPreference.isNotificationEnable)) {
            mFCMRepository.postToken(mPrefRepository.unsentFCMToken())
        }

        registerNetworkChangeReciever()

        deviceIDVisibilityHandle()

        switchToAnalytics()

        analyticsOptionColorChange()

        mapDetailsFeature()

        getCurrentVehicleStatus()

        changeVehicleMode()
    }

    var enteredSingleVehicle = 0

    private fun changeVehicleMode() {

        cardView_singleVehicleMode.setOnClickListener {

            enteredSingleVehicle++
            cardView_singleVehicleMode.background = getDrawable(R.drawable.bg_primary_corner)
            cardView_allVehicleMode.background = getDrawable(R.drawable.bg_white_corner)

            imageView_individual_vehicle_mode.setColorFilter(resources.getColor(R.color.blackTmvWhiteRvtRal))
            imageView_all_vehicle_mode.setColorFilter(resources.getColor(R.color.black))


            isVehicleModeAllVehicle = false

            if(enteredSingleVehicle == 1 && enteredInChangeCurrentVehicle == 0){
                mVehicleViewModel.changeCurrentVehicle(vehicles[0].bstid, vehicles[0].vrn, vehicles[0].bid)
                mMapDrawer?.focusOnCurrentStatus()
            }

            bottom_sheet.visibility = View.VISIBLE
            layout_fabIcon.visibility = View.VISIBLE

            mMapDrawer?.clearMap()
            //mMapDrawer?.placeMarkerAtCurrentPosition(vehicleStatus, true)
            mMapDrawer?.placeMarkerAtCurrentPosition(mCurrentVehicleStatus, true)

            mMapDrawer?.focusOnCurrentStatus()
            mMapDrawer?.mClusterManager?.clearItems()

            fabCurrentLocation.setOnClickListener {

                getLastLocation()

                val handler = Handler()
                handler.postDelayed({
                    mMapDrawer?.focusOnCurrentStatus()
                }, 3000)

            }

        }

        cardView_allVehicleMode.setOnClickListener {

            cardView_allVehicleMode.background = getDrawable(R.drawable.bg_corner_primary)
            cardView_singleVehicleMode.background = getDrawable(R.drawable.bg_corner_white)

            imageView_all_vehicle_mode.setColorFilter(resources.getColor(R.color.blackTmvWhiteRvtRal))
            imageView_individual_vehicle_mode.setColorFilter(resources.getColor(R.color.black))

            isVehicleModeAllVehicle = true

            bottom_sheet.visibility = View.GONE
            layout_fabIcon.visibility = View.GONE

            mMapDrawer?.clearMap()
            getCurrentVehicleStatus()
            setVehicleChangeStatusList()

            mMapDrawer?.focusOnBDLatLng()

            fabCurrentLocation.setOnClickListener {
                mMapDrawer?.focusOnBDLatLng()
            }

        }

        showLoading()

        bottom_sheet.visibility = View.VISIBLE
        layout_fabIcon.visibility = View.VISIBLE

        imageView_individual_vehicle_mode.setColorFilter(resources.getColor(R.color.blackTmvWhiteRvtRal))
        imageView_all_vehicle_mode.setColorFilter(resources.getColor(R.color.black))

        cardView_singleVehicleMode.background = getDrawable(R.drawable.bg_primary_corner)
        cardView_allVehicleMode.background = getDrawable(R.drawable.bg_white_corner)

        fabCurrentLocation.setOnClickListener {
            getLastLocation()

            val handler = Handler()
            handler.postDelayed({
                mMapDrawer?.focusOnCurrentStatus()
            }, 3000)

        }
        adapter = VehicleCurrentStatusAdapter(this@VehicleMonitoringActivity, onMenuClickListener)

        isSwitchFragmentAnlytics = false
    }

    fun bottomNavShow() {
        val dialogFrag = BottomNavFragment.newInstance()
        dialogFrag.show(supportFragmentManager, dialogFrag.tag)
    }

    private fun getCurrentVehicleStatus() {
        vehicleMonitoringViewModel.getVehicleCurrentStatusList().observe(this, object : Observer<List<VehicleCurrentStatusModel>> {
            override fun onChanged(data: List<VehicleCurrentStatusModel>?) {
                adapter = VehicleCurrentStatusAdapter(this@VehicleMonitoringActivity, onMenuClickListener)
                adapter.updateVehicleList(data as ArrayList<VehicleCurrentStatusModel>)
                recyclerView_vehicleStatus.adapter = adapter
            }
        })
    }

    private val onMenuClickListener = object : OnItemClickListener {
        override fun onItemClickListener(view: View, position: Int) {
            val item = vehicleMonitoringViewModel.mutableVehicleCurrentStatusList .value?.get(position) ?: return

            onItemClickedFunction(view.context, item)
        }
    }

    private fun onItemClickedFunction(context: Context, item: VehicleCurrentStatusModel) {
        VEHICLE_TYPE = item.id ?: 0

        if(VEHICLE_TYPE == ALL_VEHICLES_TYPE){
            mMapDrawer?.focusOnBDLatLng()

        }else if(VEHICLE_TYPE == MOVING_VEHICLES_TYPE){
            mMapDrawer?.focusOnBDLatLng()

        }else if(VEHICLE_TYPE == IDLE_VEHICLES_TYPE){
            mMapDrawer?.focusOnBDLatLng()

        }else if(VEHICLE_TYPE == ENGINE_OFF_VEHICLES_TYPE){
            mMapDrawer?.focusOnBDLatLng()

        }else if(VEHICLE_TYPE == OFFLINE_VEHICLES_TYPE){
            mMapDrawer?.focusOnBDLatLng()
        }

        setVehicleChangeStatusList()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun mapDetailsFeature() {

        /*fabMapDetails.setOnClickListener {
            layout_map_details.visibility = View.GONE
            layout_deviceId.visibility = View.GONE
            layout_switchVehicleMode.visibility = View.GONE
            layout_map_typeMode.visibility = View.VISIBLE }

        Layout_vehicle_monitoring.setOnTouchListener { v, event ->
            layout_map_typeMode.visibility = View.GONE

            if(!isVehicleModeAllVehicle && !isSwitchFragmentAnlytics && !isSwitchFragmentNotification) {
                bottom_sheet.visibility = View.VISIBLE
                layout_fabIcon.visibility = View.VISIBLE
                layout_map_details.visibility = View.VISIBLE
                layout_deviceId.visibility = View.VISIBLE
                layout_switchVehicleMode.visibility = View.VISIBLE

            } else if(isVehicleModeAllVehicle && !isSwitchFragmentNotification) {
                layout_map_details.visibility = View.VISIBLE
                layout_deviceId.visibility = View.VISIBLE
                layout_switchVehicleMode.visibility = View.VISIBLE
                bottom_sheet.visibility = View.GONE
                layout_fabIcon.visibility = View.GONE
            } else {
                bottom_sheet.visibility = View.GONE
                layout_fabIcon.visibility = View.GONE
            }

            return@setOnTouchListener false
        }

        imageView_mapDefault.setOnClickListener {
            mMapDrawer?.setMapType(true)
            imageView_mapDefault.strokeWidth = 4f
            imageView_mapDefault.strokeColor = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            textView_default.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            textView_satellite.setTextColor(ContextCompat.getColor(this, R.color.black))

            imageView_mapSatellite.strokeWidth = 0f
            imageView_mapSatellite.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
        }

        imageView_mapSatellite.setOnClickListener {
            mMapDrawer?.setMapType(false)
            imageView_mapSatellite.strokeWidth = 4f
            imageView_mapSatellite.strokeColor = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            textView_satellite.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            textView_default.setTextColor(ContextCompat.getColor(this, R.color.black))

            imageView_mapDefault.strokeWidth = 0f
            imageView_mapDefault.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
        }

        cardView_dayMode.setOnClickListener {
            cardView_dayMode.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            cardView_nightMode.setCardBackgroundColor(resources.getColor(R.color.nightModeColorBackground))
            textView_light.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            textView_Dark.setTextColor(ContextCompat.getColor(this, R.color.black))

            mMapDrawer?.setNightMode(false)
        }

        cardView_nightMode.setOnClickListener {
            cardView_dayMode.setCardBackgroundColor(resources.getColor(R.color.nightModeColorBackground))
            cardView_nightMode.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            textView_light.setTextColor(ContextCompat.getColor(this, R.color.black))
            textView_Dark.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))

            mMapDrawer?.setNightMode(true)
        }*/
    }

    private fun analyticsOptionColorChange() {
        textView_analytics.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
        textView_liveMap.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
        textView_vehicles.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
        textView_notifications.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
    }

    private fun switchToAnalytics() {
        fabAnalytics.setOnClickListener {
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                Toast.makeText(this, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (terminalId == null || terminalId == "" || terminalId == "null") {
                    Toast.makeText(this@VehicleMonitoringActivity, "Terminal id not found, Please try again", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                isSwitchFragmentAnlytics = true
                fragment_container.visibility = View.VISIBLE
                bottom_sheet.visibility = View.GONE
                layout_fabIcon.visibility = View.GONE
                layout_map_details.visibility = View.GONE
//                layout_map_typeMode.visibility = View.GONE
                layout_deviceId.visibility = View.GONE

                fabAnalytics.visibility = View.GONE
                textView_analytics.visibility = View.VISIBLE
                textView_liveMap.visibility = View.GONE
                fabMonitor.visibility = View.VISIBLE
                fabVehicles.visibility = View.VISIBLE
                textView_vehicles.visibility = View.GONE
                layout_device_id.visibility = View.INVISIBLE
                recyclerView_vehicleStatus.visibility = View.GONE

                cardView_current_location.visibility = View.GONE
//                cardView_map_details.visibility = View.GONE
                cardView_vehicleMode.visibility = View.GONE

                layout_notifications.visibility = View.VISIBLE
                textView_notifications.visibility = View.GONE

                //supportFragmentManager.beginTransaction().add(R.id.fragment_container, AnalyticsFragment()).commit()
                val bundle = Bundle()
                bundle.putString("cookie", mPrefRepository.cookie)
                bundle.putString("userName", userName)

                val analyticsFragment = AnalyticsFragment()
                analyticsFragment.arguments = bundle
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, analyticsFragment).commit()
        }

    }

    private fun deviceIDVisibilityHandle() {
        fabVehicles.setOnClickListener {

            isSwitchFragmentAnlytics = false
            isSwitchFragmentNotification = false

            bottomNavShow()

            analyticsToHome2Activity()

            layout_device_id.visibility = View.VISIBLE
            cardView_current_location.visibility = View.VISIBLE
//            cardView_map_details.visibility = View.VISIBLE
            cardView_vehicleMode.visibility = View.VISIBLE
            textView_liveMap.visibility = View.GONE
            fabMonitor.visibility = View.VISIBLE
            textView_vehicles.visibility = View.VISIBLE
            fabVehicles.visibility = View.GONE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE
//            layout_map_typeMode.visibility = View.GONE
            layout_map_details.visibility = View.VISIBLE
            layout_deviceId.visibility = View.VISIBLE
            layout_notifications.visibility = View.VISIBLE
            textView_notifications.visibility = View.GONE

            cardView_vehicleMode.visibility = View.GONE

            isVehicleModeAllVehicle = false

            enteredSingleVehicle++

            /*cardView_singleVehicleMode.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            cardView_allVehicleMode.setCardBackgroundColor(resources.getColor(R.color.white))
            */
            cardView_singleVehicleMode.background = getDrawable(R.drawable.bg_primary_corner)
            cardView_allVehicleMode.background = getDrawable(R.drawable.bg_white_corner)

            imageView_individual_vehicle_mode.setColorFilter(resources.getColor(R.color.blackTmvWhiteRvtRal))
            imageView_all_vehicle_mode.setColorFilter(resources.getColor(R.color.black))

            if(enteredSingleVehicle == 1 && enteredInChangeCurrentVehicle == 0){
                mVehicleViewModel.changeCurrentVehicle(vehicles[0].bstid, vehicles[0].vrn, vehicles[0].bid)
                mMapDrawer?.focusOnCurrentStatus()
            }

            bottom_sheet.visibility = View.VISIBLE
            layout_fabIcon.visibility = View.VISIBLE

            mMapDrawer?.clearMap()
            //mMapDrawer?.placeMarkerAtCurrentPosition(vehicleStatus, true)
            mMapDrawer?.placeMarkerAtCurrentPosition(mCurrentVehicleStatus, true)

            mMapDrawer?.focusOnCurrentStatus()
            mMapDrawer?.mClusterManager?.clearItems()

            fabCurrentLocation.setOnClickListener {
                getLastLocation()

                val handler = Handler()
                handler.postDelayed({
                    mMapDrawer?.focusOnCurrentStatus()
                }, 3000)
            }


        }

        fabMonitor.setOnClickListener {

            isSwitchFragmentAnlytics = false
            isSwitchFragmentNotification = false

            analyticsToHome2Activity()

            layout_device_id.visibility = View.INVISIBLE
            cardView_current_location.visibility = View.VISIBLE
//            cardView_map_details.visibility = View.VISIBLE
            cardView_vehicleMode.visibility = View.VISIBLE
            textView_liveMap.visibility = View.VISIBLE
            fabMonitor.visibility = View.GONE
            textView_vehicles.visibility = View.GONE
            fabVehicles.visibility = View.VISIBLE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE
//            layout_map_typeMode.visibility = View.GONE
            layout_map_details.visibility = View.VISIBLE
            layout_deviceId.visibility = View.VISIBLE
            layout_switchVehicleMode.visibility = View.VISIBLE
            layout_notifications.visibility = View.VISIBLE
            textView_notifications.visibility = View.GONE

        }

        textView_vehicles.setOnClickListener {

            isSwitchFragmentAnlytics = false
            isSwitchFragmentNotification = false

            analyticsToHome2Activity()

            layout_device_id.visibility = View.VISIBLE
            cardView_current_location.visibility = View.VISIBLE
//            cardView_map_details.visibility = View.VISIBLE
            cardView_vehicleMode.visibility = View.VISIBLE
            textView_liveMap.visibility = View.GONE
            fabMonitor.visibility = View.VISIBLE
            textView_vehicles.visibility = View.VISIBLE
            fabVehicles.visibility = View.GONE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE
//            layout_map_typeMode.visibility = View.GONE
            layout_map_details.visibility = View.VISIBLE
            layout_deviceId.visibility = View.VISIBLE
            cardView_vehicleMode.visibility = View.GONE
            layout_notifications.visibility = View.VISIBLE
            textView_notifications.visibility = View.GONE

        }

    }

    private fun switchToNotificationVisibilityHandle () {
        isSwitchFragmentNotification = true

        fragment_container.visibility = View.VISIBLE

        bottom_sheet.visibility = View.GONE
        layout_fabIcon.visibility = View.GONE

        layout_switchVehicleMode.visibility = View.GONE
        cardView_vehicleMode.visibility = View.GONE
        layout_map_details.visibility = View.GONE
        layout_deviceId.visibility = View.GONE

        recyclerView_vehicleStatus.visibility = View.GONE

        layout_notifications.visibility = View.GONE
        textView_notifications.visibility = View.VISIBLE
        fabMonitor.visibility = View.VISIBLE
        textView_liveMap.visibility = View.GONE
        fabVehicles.visibility = View.VISIBLE
        textView_vehicles.visibility = View.GONE
        fabAnalytics.visibility = View.VISIBLE
        textView_analytics.visibility = View.GONE
    }

    private fun analyticsToHome2Activity() {
        isSwitchFragmentAnlytics = false
        if(isVehicleModeAllVehicle == false){
            bottom_sheet.visibility = View.VISIBLE
            layout_fabIcon.visibility = View.VISIBLE
        }else{
            bottom_sheet.visibility = View.GONE
            layout_fabIcon.visibility = View.GONE
        }
        layout_map_details.visibility = View.VISIBLE
        fragment_container.visibility = View.GONE
        recyclerView_vehicleStatus.visibility = View.VISIBLE
    }

    private fun registerNetworkChangeReciever() {
        val br = NetworkChangeReceiver()
        val filter = IntentFilter().apply {
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
            addAction("android.net.wifi.WIFI_STATE_CHANGED")
        }
        registerReceiver(br, filter)
    }

    private fun observerNotifications() {
        mNotificationViewModel.fetchUnreadNotificationCount().observe(this, Observer { data ->
            if ((data?.data ?: 0) == 0) {
                txtToolbarNotificationNumber.visibility = View.GONE
            } else {
                txtToolbarNotificationNumber.visibility = View.VISIBLE
                val count = data.data ?: 0
                txtToolbarNotificationNumber.text = if (count > 9) "9+" else count.toString()
            }
        })
    }

    private fun selectNextVehicle() {
        mVehicleViewModel.selectNextVehicle()
    }

    private fun selectPreviousVehicle() {
        mVehicleViewModel.selectPreviousVehicle()
    }

    private fun hideNotificationFabIfRequired() {
        when (userSource) {
            UserSource.VERSION_3 -> {
                fabNotification.visibility = View.VISIBLE
                txtToolbarNotificationNumber.visibility = View.VISIBLE
            }
            UserSource.VERSION_2 -> {
                fabNotification.visibility = View.GONE
                txtToolbarNotificationNumber.visibility = View.GONE
            }
            else -> {
            }
        }
    }

    private fun hideNecessaryNavigationItems() {
        when (userSource) {
            UserSource.VERSION_3 -> {
                navigationDrawerView.menu.findItem(R.id.navigation_support).isVisible = true
                navigationDrawerView.menu.findItem(R.id.navigation_expense).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_pay_bill).isVisible = false

                //TODO: When Notification is implemented remove this
                navigationDrawerView.menu.findItem(R.id.navigation_notification).isVisible = false
            }
            UserSource.VERSION_2 -> {
                navigationDrawerView.menu.findItem(R.id.navigation_hourly_distance).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_notification).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_monthly_report).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_location_report).isVisible = false
            }
            else -> {
            }
        }
    }

    private val vehiclePagerAdapter = VehiclePagerAdapter(object : OnItemClickCallback<Terminal> {
        override fun onClick(model: Terminal, position: Int) {
            viewPager.currentItem = position
            setCurrentViewPagerPage(position)
            mVehicleViewModel.changeCurrentVehicle(model.bstid, model.vrn, model.bid)
        }
    })

    private fun initConstrainViews() {

        val behavior = BottomSheetBehaviorGoogleMapsLike.from<View>(bottom_sheet)
        behavior.addBottomSheetCallback(object :
                BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED    -> Log.d("bottomsheet-",
                            "STATE_COLLAPSED")
                    BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING     -> Log.d("bottomsheet-",
                            "STATE_DRAGGING")
                    BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED     -> {
                        Log.d("bottomsheet-", "STATE_EXPANDED")
                    }
                    BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT -> Log.d("bottomsheet-",
                            "STATE_ANCHOR_POINT")
                    BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN       -> Log.d("bottomsheet-",
                            "STATE_HIDDEN")
                    else                                                 -> Log.d("bottomsheet-", "STATE_SETTLING")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        viewPager.adapter = vehiclePagerAdapter

        mVehicleViewModel.fetch(false)

        mVehicleViewModel.getVehicles().observe(this, Observer {
            if (it.isEmpty()) return@Observer

            updateVehicleList(it as ArrayList<Terminal>)
        })

        viewPager.offscreenPageLimit = 1
        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(
                R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            page.scaleY = 1 - (0.25f * abs(position))
            page.scaleX = 1 - (0.25f * abs(position))
        }

        viewPager.setPageTransformer(pageTransformer)
        setCurrentViewPagerPage(0)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentViewPagerPage(position)
                if (vehicles.size > position) {
                    val model = vehicles[position]
                    mVehicleViewModel.changeCurrentVehicle(model.bstid, model.vrn, model.bid)
                }
            }
        })


        val itemDecoration = HorizontalMarginItemDecoration(this,
                R.dimen.viewpager_current_item_horizontal_margin)
        viewPager.addItemDecoration(itemDecoration)

        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED

        listReportButtons.adapter = ReportButtonAdapter(this)
        listReportButtons.layoutManager = GridLayoutManager(this, 2)

    }

    private fun setVehicleChangeStatusList() {
        adapter.setSelectedView(VEHICLE_TYPE)

        if(VEHICLE_TYPE == ALL_VEHICLES_TYPE){
            mMapDrawer?.setVehicleList(vehicles, true)

        }else if(VEHICLE_TYPE == MOVING_VEHICLES_TYPE){
            mMapDrawer?.setVehicleList(movingVehicles, true)

        }else if(VEHICLE_TYPE == IDLE_VEHICLES_TYPE){
            mMapDrawer?.setVehicleList(idleVehicles, true)

        }else if(VEHICLE_TYPE == ENGINE_OFF_VEHICLES_TYPE){
            mMapDrawer?.setVehicleList(engineOffVehicles, true)

        }else if(VEHICLE_TYPE == OFFLINE_VEHICLES_TYPE){
            mMapDrawer?.setVehicleList(offlineVehicles, true)
        }
    }

    var enteredupdateVehicleList = 0

    private fun updateVehicleList(data: ArrayList<Terminal>) {
        vehicles = data

        enteredupdateVehicleList++
        if(enteredupdateVehicleList == 1){
            mAnalyticsViewModel.vehicleSelected()
            mVehicleViewModel.changeCurrentVehicle(vehicles[0].bstid, vehicles[0].vrn, vehicles[0].bid)
            selectedVehicleLatLng = mMapDrawer?.selectedVehicleLatLng(CurrentVehicleChangeEvent(vehicles[0].bstid, vehicles[0].vrn))
            mMapDrawer?.setVehicleList(vehicles, false)
            mMapDrawer?.mClusterManager?.clearItems()
        }

        offlineVehicles.clear()
        idleVehicles.clear()
        movingVehicles.clear()
        engineOffVehicles.clear()

        for (item in vehicles) {
            val lastUpdatedDate = item.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")

            var updatedDate = try {
                DateTime.parse(lastUpdatedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
            } catch (ex: Exception) {
                null
            }

            if (! item.isSuspended()) {
                if (updatedDate?.isBefore(DateTime.now().minusDays(1)) == true) {
                    offlineVehicles.add(item)

                } else if (item.terminalDataIsAccOnLast == "1" && item.terminalDataVelocityLast?.toDouble() ?: 0.0 == 0.0) {
                    idleVehicles.add(item)

                } else if (item.terminalDataIsAccOnLast == "1") {
                    movingVehicles.add(item)

                } else {
                    engineOffVehicles.add(item)

                }
            }

            if(txtCurrentBstId.text.toString() == item.bstId){
                vehicleStatus.bid = item.bid.toIntOrNull() ?: 0
                vehicleStatus.bstid = item.bstid
                val locationResponse = LocationResponse()
                if (! item.isSuspended()) {
                    vehicleStatus.engineStatus = if (item.terminalDataIsAccOnLast == "1") "ON" else "OFF"
                    locationResponse.latitude = item.terminalDataLatitudeLast
                    locationResponse.longitude = item.terminalDataLongitudeLast
                    locationResponse.place = item.geoLocationName + " (" + item.geoLocationPositionLandmarkDistanceMeter + " m)"
                    vehicleStatus.speed = String.format("%.1f",
                            item.terminalDataVelocityLast?.toFloatOrNull())
                    vehicleStatus.updatedAt = item.terminalDataTimeLast?.toString(
                            "yyyy-MM-dd HH:mm:ss")
                } else {
                    vehicleStatus.updatedAt = ""
                    vehicleStatus.engineStatus = "--"
                    locationResponse.place = "--"
                    vehicleStatus.speed = "--"
                }
                vehicleStatus.location = locationResponse
                vehicleStatus.vrn = item.vrn

                selectedVehicleLatLng = LatLng(vehicleStatus?.location?.latitude?.toDouble()
                        ?: 0.00, vehicleStatus?.location?.longitude?.toDouble() ?: 0.00)
            }
        }

        vehicleMonitoringViewModel.setCurrentVehicleStatus(vehicles, offlineVehicles, idleVehicles, movingVehicles, engineOffVehicles)

        if(isVehicleModeAllVehicle){
            mMapDrawer?.clearMap()
            setVehicleChangeStatusList()

        } else if(enteredupdateVehicleList == 1) {

            if (!isVehicleModeAllVehicle) {
                dismissDialog()
                mMapDrawer?.focusOnCurrentStatus()

                mMapDrawer?.placeMarkerAtCurrentPosition(vehicleStatus, isFirstTime = mCurrentVehicleStatus == null)

                if (mCurrentVehicleStatus?.bstid != vehicleStatus?.bstid) {
                    mMapDrawer?.focusOnCurrentStatus()
                }

                mCurrentVehicleStatus = vehicleStatus
            }
        }

        if (! vehicles.map(Terminal::terminalID).contains(
                        mPrefRepository.currentVehicleTerminalId.toIntOrNull())) {
            selectNextVehicle()
        }
        vehiclePagerAdapter.setData(data)

        terminalId_TextView.text = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()
        terminalId = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()
    }

    override fun onClick(model: ReportButtonModel) {
        when (model.slug) {
            "Hourly Report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_HOURLY_DISTANCE_REPORT)
            }
            "Engine report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_ENGINE_ON_REPORT)
            }
            "Vehicle Route Analytics".toLowerCase().replace(" ", "-") -> {
                startActivity(Intent(this, VehicleRouteAnalyticsActivity::class.java))
            }
            "Distance Report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_DISTANCE_REPORT)
            }
            "Monthly Report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_MONTHLY_DISTANCE_REPORT)
            }
            "Pay Bill".toLowerCase().replace(" ", "-") -> {
                BillingActivity.intent(this)
            }
            "Notifications".toLowerCase().replace(" ", "-") -> {
                switchToNotificationVisibilityHandle()

                var notificationFragment: NotificationFragment = NotificationFragment()
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, notificationFragment).commit()
                //FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
            }
            "Subscription".toLowerCase().replace(" ", "-") -> {
            }
            "Location Report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_LOCATION_REPORT)
            }
            "Disarm Engine".toLowerCase().replace(" ", "-") -> {
                SecureModeActivity.intent(this)
            }
            "Speed report".toLowerCase().replace(" ", "-") -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_SPEED_REPORT)
            }
        }
    }

    private fun setCurrentViewPagerPage(position: Int) {
        txtViewPagerNumber.text = StringBuilder().apply {
            this.append(position + 1)
            this.append("/")
            this.append(viewPager.adapter?.itemCount ?: 0)
        }.toString()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        runOnUiThread {
            Log.e("TAG2", "onVehicleChange: ${mPrefRepository.currentVehicleTerminalId}")

            fetchCurrentLocation()
            fetchCurrentTravelledDistance()
            changeCurrentVehicle(event)
            observeCurrentTravelledDistance()
            mMapDrawer?.removePolyLine()
            mMapDrawer?.clearMap()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetworkStatusChange(event: NetworkConnectivityEvent) {
        runOnUiThread {
            txtNoInternetText.visibility = if (event.connected) View.GONE else View.VISIBLE
        }
    }

    var enteredInChangeCurrentVehicle = 0

    private fun changeCurrentVehicle(event: CurrentVehicleChangeEvent) {
        if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
            showLoading()
        } else{
            Toast.makeText(this, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()
        }

        enteredInChangeCurrentVehicle++
        vehicleUpdateType = VEHICLE_UPDATE_TYPE
        if(isVehicleModeAllVehicle == true){
            setVehicleChangeStatusList()
        }
        val indexOfFirst = vehicles.indexOfFirst { it.bstid == event.bstId }
        viewPager.currentItem = if (indexOfFirst >= 0) indexOfFirst else 0
        txtCurrentBstId.text = event.bstId
        txtVehicleAliasName.text = event.bstId
        txtVehicleId.text = event.vrn
        txtVehicleLastLocation.text = "N/A"
        txtVehicleLastUpdatedAt.text = "--:--"
        txtEngineStatus.text = "--"
        imgEngineStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.engine_off))
        txtVehicleSpeed.text = "--" + "KM/H"

        btnPayNow.visibility = View.GONE
        if (vehicles.isEmpty()) {
            containerPayment.visibility = View.GONE
        } else {
            val terminal = vehicles[if (indexOfFirst >= 0) indexOfFirst else 0]
            containerPayment.visibility = if (!terminal.isSuspended()) View.GONE
            else View.VISIBLE
            txtAlert.text = "Suspended! Please contact customer care."
        }

        mMapDrawer?.focusOnCurrentStatusFromVehicleMonitoring(event)

        //mMapDrawer?.focusOnCurrentStatus()

        selectedVehicleLatLng = mMapDrawer?.selectedVehicleLatLng(event)

        Log.d("jhbuy", "${mMapDrawer?.isClusterOnClicked}")

        if(isVehicleModeAllVehicle == true){
            if(mMapDrawer?.isClusterOnClicked == true){
                bottom_sheet.visibility = View.VISIBLE
                layout_fabIcon.visibility = View.VISIBLE
            }else{
                bottom_sheet.visibility = View.GONE
                layout_fabIcon.visibility = View.GONE
            }
        }


    }

    fun getLastLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
        fusedLocationClient?.lastLocation
                ?.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        Log.d("ygiu", "${location.latitude} ${location.longitude}")
                        mMapDrawer?.placeCurrentLocationMarkerOnMap(LatLng(location.latitude, location.longitude))
                    }
                }
    }

    private fun showLoading() {
        dismissDialog()
        mDialog = DialogHelper.getLoadingDailog(this, getString(R.string.msg_hold_on),
                getString(R.string.msg_loading))
                ?.show()
    }

    private fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }

    private fun showDialogError(msg: String) {
        dismissDialog()
        mDialog = DialogHelper.getMessageDialog(this, getString(R.string.title_error), msg)
                ?.positiveText(getString(R.string.action_ok))
                ?.show()
    }
}