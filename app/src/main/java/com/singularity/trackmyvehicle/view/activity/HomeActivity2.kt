package com.singularity.trackmyvehicle.view.activity

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.gaurav.gesto.OnGestureListener
import com.google.android.material.navigation.NavigationView
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.admin.AdminNavigation
import com.singularity.trackmyvehicle.admin.IAdminNavigation
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.model.event.NetworkConnectivityEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.utils.NetworkChangeReceiver
import com.singularity.trackmyvehicle.utils.getRelativeTimeFromNow
import com.singularity.trackmyvehicle.view.adapter.ReportButtonAdapter
import com.singularity.trackmyvehicle.view.adapter.ReportButtonModel
import com.singularity.trackmyvehicle.view.adapter.VehiclePagerAdapter
import com.singularity.trackmyvehicle.view.customview.behaviour.BottomSheetBehaviorGoogleMapsLike
import com.singularity.trackmyvehicle.view.decoration.HorizontalMarginItemDecoration
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewSegment.CurrentStatusTicker
import com.singularity.trackmyvehicle.view.viewSegment.HasCurrentVehicleStatus
import com.singularity.trackmyvehicle.view.viewSegment.UsesMapWithDrawer
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.NotificationViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home_2.*
import kotlinx.android.synthetic.main.layout_content_toolbar.*
import kotlinx.android.synthetic.main.layout_current_location.*
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import kotlinx.android.synthetic.main.layout_device_id_toolbar.*
import kotlinx.android.synthetic.main.layout_home_content.*
import kotlinx.android.synthetic.main.layout_map_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs


/**
 * Created by Sadman Sarar on 2020-01-15.
 */
class HomeActivity2 : AppCompatActivity(),
        HasCurrentVehicleStatus,
        UsesMapWithDrawer,
        CurrentStatusTicker,
        OnItemClickCallback<ReportButtonModel>,
        AdminNavigation by IAdminNavigation() {


    private var mCurrentDistanceTravelLiveData: LiveData<TerminalAggregatedData>? = null
    private var menuItemId: Int = -1
    private var vehicles = listOf<Terminal>()

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        menuItemId = item.itemId
        when (item.itemId) {

            R.id.navigation_home -> {
                bottom_sheet.visibility = View.VISIBLE
                layout_fabIcon.visibility = View.VISIBLE
                fragment_container.visibility = View.GONE

                fabAnalytics.visibility = View.VISIBLE
                textView_analytics.visibility = View.GONE
                textView_liveMap.visibility = View.VISIBLE
                fabMonitor.visibility = View.GONE
                textView_vehicles.visibility = View.GONE
                fabVehicles.visibility = View.VISIBLE

                startActivity(Intent(this, HomeActivity2::class.java))
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_route -> {
                //var intent = Intent(this, VehicleRouteActivity::class.java)
                //startActivity(intent)
                //startActivity(Intent(this, HomeActivity::class.java))
                //startActivity(Intent(this, VehicleRouteAnalyticsActivity::class.java))
                var intent = Intent(this, VehicleRouteAnalyticsActivity :: class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
                closeDrawer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_account -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_ACCOUNT)
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
                FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
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
                    gotoSwitchUserScreen(this@HomeActivity2)
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

    override var mCurrentVehicleStatusLiveData: LiveData<Resource<VehicleStatus>>? = null
    override var mCurrentStatusHandler: Handler = Handler()
    override var mCurrentStatusRunnable: Runnable = currentStatusRunnable()
    private var mCurrentVehicleStatus: VehicleStatus? = null

    private var mDialog: MaterialDialog? = null

    var terminalId : String? = ""

    var userName : String = ""

//    override fun getActivity(): Activity {
//        return this
//    }

    override fun updateCurrentVehicleStatus(data: VehicleStatus?) {

        Log.e("Map Drawer", "placeVehicleAt: ${data?.location?.latitude} : ${data?.location?.longitude} ")

        if (mCurrentDistanceTravelLiveData == null || mCurrentDistanceTravelLiveData?.hasObservers() == false) {
            observeCurrentTravelledDistance()
        }

        if (data?.bstid != mVehicleViewModel.mPrefRepository.currentVehicle()) {
            return
        }
        mMapDrawer?.placeMarkerAtCurrentPosition(data, isFirstTime = mCurrentVehicleStatus == null)
        if (mCurrentVehicleStatus?.bstid != data?.bstid) {
            mMapDrawer?.focusOnCurrentStatus()
        }

        mCurrentVehicleStatus = data

        // TODO: Update texts
        txtCurrentBstId.text = data?.bstid
        txtVehicleAliasName.text = data?.bstid
        Log.d("CurrVeh", txtVehicleAliasName.text.toString())
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
                if ((lastUpdated?.isBefore(DateTime.now().minusDays(
                                1)) == true) || data?.engineStatus?.contains(
                                "--") == true) ContextCompat.getDrawable(
                        this,
                        R.drawable.engine_disable)
                else if (data?.engineStatus == "ON" && (data.speed?.toFloatOrNull()
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

    override fun onResume() {
        super.onResume()
        startTicker()
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

    override fun onPause() {
        super.onPause()
        stopTicker()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        setContentView(R.layout.activity_home_2)

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
            val currentVehicleStatus = mMapDrawer?.currentVehicleStatus()
            val lat = currentVehicleStatus?.location?.latitude?.toDoubleOrNull() ?: 0.0
            val lon = currentVehicleStatus?.location?.longitude?.toDoubleOrNull() ?: 0.0
            if (currentVehicleStatus != null && lat != 0.0 && lon != 0.0) {
                val url = "https://www.google.com/maps/dir/?api=1&origin=$lat,$lon&travelmode=driving"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toasty.error(this, "Location not available").show()
            }
        }

        virtualWatchman.setOnClickListener {

            if (mNetworkAvailabilityChecker.isNetworkAvailable())
              {
                  if (terminalId == null || terminalId == "" || terminalId == "null"){
                      //return@setOnClickListener
                      Toast.makeText(this@HomeActivity2, "Terminal id not found, Please try again", Toast.LENGTH_SHORT).show()
                  }else{
                      Log.d("uyjhfh", terminalId ?: "asdf")

                      val intent = Intent(this@HomeActivity2, VirtualWatchmanSet::class.java)
                      intent.putExtra("Terminal_ID", terminalId)
                      startActivity(intent)
                  }

              }

            else
              {
                Toast.makeText(this, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()

              }



        }

        fabNotification.setOnClickListener {
            layout_device_id.visibility = View.GONE
            FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
        }

        fetchCurrentLocation()

        initConstrainViews()

        mMapDrawer?.setTraffic(true)
        mMapDrawer?.placeMarkerAtCurrentPosition(mCurrentVehicleStatus, true)
        mMapDrawer?.removePolyLine()

        fabCurrentLocation.setOnClickListener { mMapDrawer?.focusOnCurrentStatus() }

        imgChangeVehicle.setOnClickListener {
            val dialogFrag = BottomNavFragment.newInstance()
            dialogFrag.show(supportFragmentManager, dialogFrag.tag)
        }

//        fetchCurrentLocation()
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

        registerNetworkChangeReciever()

        deviceIDVisibilityHandle()

        switchToAnalytics()

        analyticsOptionColorChange()

    }

    private fun analyticsOptionColorChange() {
        textView_analytics.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
        textView_liveMap.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
        textView_vehicles.setTextColor(resources.getColor(R.color.analyticsButtonSpecificColor))
    }

    private fun switchToAnalytics() {
        fabAnalytics.setOnClickListener {
            if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
                  if (terminalId == null || terminalId == "" || terminalId == "null"){
                      //return@setOnClickListener
                      Toast.makeText(this@HomeActivity2, "Terminal id not found, Please try again", Toast.LENGTH_SHORT).show()
                  }else{
                      fragment_container.visibility = View.VISIBLE
                      bottom_sheet.visibility = View.GONE
                      layout_fabIcon.visibility = View.GONE

                      fabAnalytics.visibility = View.GONE
                      textView_analytics.visibility = View.VISIBLE
                      textView_liveMap.visibility = View.GONE
                      fabMonitor.visibility = View.VISIBLE
                      fabVehicles.visibility = View.VISIBLE
                      textView_vehicles.visibility = View.GONE
                      layout_device_id.visibility = View.INVISIBLE

                      cardView_current_location.visibility = View.GONE

                      //supportFragmentManager.beginTransaction().add(R.id.fragment_container, AnalyticsFragment()).commit()
                      var bundle : Bundle = Bundle()
                      bundle.putString("cookie", mPrefRepository.cookie)
                      bundle.putString("userName",userName)
                      var analyticsFragment : AnalyticsFragment = AnalyticsFragment()
                      analyticsFragment.arguments = bundle
                      supportFragmentManager.beginTransaction().add(R.id.fragment_container, analyticsFragment).commit()
                  }
            }else {
                Toast.makeText(this, "Internet Connection is not Available", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun deviceIDVisibilityHandle() {
        fabVehicles.setOnClickListener {

            analyticsToHome2Activity()

            layout_device_id.visibility = View.VISIBLE
            cardView_current_location.visibility = View.VISIBLE
            textView_liveMap.visibility = View.GONE
            fabMonitor.visibility = View.VISIBLE
            textView_vehicles.visibility = View.VISIBLE
            fabVehicles.visibility = View.GONE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE

        }

        fabMonitor.setOnClickListener {

            analyticsToHome2Activity()

            layout_device_id.visibility = View.INVISIBLE
            cardView_current_location.visibility = View.VISIBLE
            textView_liveMap.visibility = View.VISIBLE
            fabMonitor.visibility = View.GONE
            textView_vehicles.visibility = View.GONE
            fabVehicles.visibility = View.VISIBLE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE

        }

        textView_vehicles.setOnClickListener {

            analyticsToHome2Activity()

            layout_device_id.visibility = View.VISIBLE
            cardView_current_location.visibility = View.VISIBLE
            textView_liveMap.visibility = View.GONE
            fabMonitor.visibility = View.VISIBLE
            textView_vehicles.visibility = View.VISIBLE
            fabVehicles.visibility = View.GONE
            fabAnalytics.visibility = View.VISIBLE
            textView_analytics.visibility = View.GONE

        }

    }

    private fun analyticsToHome2Activity() {
        bottom_sheet.visibility = View.VISIBLE
        layout_fabIcon.visibility = View.VISIBLE
        fragment_container.visibility = View.GONE

        //startActivity(Intent(this, HomeActivity2::class.java))
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
                navigationDrawerView.menu.findItem(R.id.navigation_hourly_distance)
                        .isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_notification).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_monthly_report).isVisible = false
                navigationDrawerView.menu.findItem(R.id.navigation_location_report)
                        .isVisible = false
            }
            else -> {
            }
        }
    }

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


        val vehiclePagerAdapter = VehiclePagerAdapter(object : OnItemClickCallback<Terminal> {
            override fun onClick(model: Terminal, position: Int) {
                viewPager.currentItem = position
                setCurrentViewPagerPage(position)
                mVehicleViewModel.changeCurrentVehicle(model.bstid, model.vrn, model.bid)
            }
        })
        viewPager.adapter = vehiclePagerAdapter

        mVehicleViewModel.fetch(false)

        mVehicleViewModel.getVehicles().observe(this, Observer {
            vehicles = it
            if (!vehicles.map(Terminal::terminalID).contains(
                            mPrefRepository.currentVehicleTerminalId.toIntOrNull())) {

                selectNextVehicle()
            }
            vehiclePagerAdapter.setData(it)
            Log.e("Home", "initConstrainViews: entered")
            Log.d("terID", mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString())
            terminalId_TextView.text = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()
            terminalId = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()
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
                FragmentActivity.startActivity(this, FragmentActivity.TAG_NOTIFICATION)
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

    private fun changeCurrentVehicle(event: CurrentVehicleChangeEvent) {
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

