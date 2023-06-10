package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.admin.AdminNavigation
import com.singularity.trackmyvehicle.admin.IAdminNavigation
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendExpiredDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendedMaintenanceDialog
import com.singularity.trackmyvehicle.view.fragment.MapFragment
import com.singularity.trackmyvehicle.view.fragment.NotificationFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewSegment.CurrentStatusTicker
import com.singularity.trackmyvehicle.view.viewSegment.HasCurrentVehicleStatus
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.NotificationViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_monitoring.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_content_toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

const val OPTION_MONITOR = 1
const val OPTION_LIST = 2
const val OPTION_NOTIFICATION = 3
const val OPTION_ANALYTICS = 4

class MonitoringActivity : BaseActivity(),
        HasCurrentVehicleStatus,
        CurrentStatusTicker,
        AdminNavigation by IAdminNavigation() {

    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mFCMRepository: FCMRepository
    @Inject
    lateinit var appPreference: AppPreference
    @Inject
    lateinit var mFirebaseAnalyticsViewModel: AnalyticsViewModel
    @Inject
    lateinit var mProfileViewModel: ProfileViewModel
    @Inject
    lateinit var userSource: UserSource
    @Inject
    lateinit var mNotificationViewModel: NotificationViewModel
    @Inject
    lateinit var mVehicleRepository: VehicleRepository
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    @Inject
    lateinit var mNetworkAvailabilityChecker: NetworkAvailabilityChecker

    override var mCurrentStatusHandler: Handler = Handler()
    override var mCurrentStatusRunnable: Runnable = currentStatusRunnable()

    private var isShowAllVehicle = false

    var currentVehicleLocation = VehicleStatus()

    var selectedOption = -1
    var mapFragment: MapFragment? = null
    var isFirstTime = true
    var isSetObserver = false

    var terminals: ArrayList<Terminal> = ArrayList()

    private var mDialog: MaterialDialog? = null
    var mMapDrawer: MapDrawer? = null

/*    lateinit var suspendView: RelativeLayout
    lateinit var blurView: ImageView*/

    val suspendExpiredDialog = SuspendExpiredDialog()

    val suspendMaintenanceDialog = SuspendedMaintenanceDialog()
    val suspendDialog = SuspendDialog()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        startTicker()

        setContentView(R.layout.activity_monitoring)


        hideNecessaryNavigationItems()
        hideNotificationFabIfRequired()

        navigationDrawerView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        imgDrawerMenu.setOnClickListener {
            container.openDrawer(GravityCompat.START)
        }

        layout_notifications.setOnClickListener {
            setupTopOptions(OPTION_NOTIFICATION)
        }

        fabAnalytics.setOnClickListener {
            setupTopOptions(OPTION_ANALYTICS)
        }

        fabMonitor.setOnClickListener {
            setupTopOptions(OPTION_MONITOR)
        }

        fabVehicles.setOnClickListener {
            setupTopOptions(OPTION_LIST)
        }

        if (intent.extras?.getBoolean("isAfterLogin", false) == true) {
            setupVehicleObserver()
        }

        fetchCurrentTravelledDistance()

        setupTopOptions(OPTION_MONITOR)

        observerNotifications()

        if (mFCMRepository.shouldSendFCMToken() && appPreference.getBoolean(AppPreference.isNotificationEnable)) {
            mFCMRepository.postToken(mPrefRepository.unsentFCMToken())
        }



        TerminalSuspendedVehicle()


    /*suspendView =  findViewById<RelativeLayout>(R.id.suspendView)
      blurView =  findViewById<ImageView>(R.id.blur_view)*/
    }



  @RequiresApi(Build.VERSION_CODES.N)
  private  fun TerminalSuspendedVehicle(){


      //catch suspend here
      if (!isShowAllVehicle) {
          mVehicleViewModel.getCurrentVehicle {

               try {
                   if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.robi.robitrackervts")){

                       if(it?.terminalAssignmentIsSuspended == "1"){

                           var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                           if(TerminalTimeLast == ""  || TerminalTimeLast == "null" || TerminalTimeLast == null){

                               suspendView.visibility = View.VISIBLE
                               blur_view.visibility = View.VISIBLE

                               vehicleModeCard.visibility = View.GONE
                               mapDetailsCard.visibility = View.GONE
                               refreshCard.visibility =View.GONE
                               currentLocationCard.visibility = View.GONE
                               layout_fabIcon.visibility = View.GONE

                               suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")

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
                                       blur_view.visibility = View.VISIBLE

                                       vehicleModeCard.visibility = View.GONE
                                       mapDetailsCard.visibility = View.GONE
                                       refreshCard.visibility =View.GONE
                                       currentLocationCard.visibility = View.GONE
                                       layout_fabIcon.visibility = View.GONE

                                       suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")

                                       //   Toast.makeText(requireContext(), "SuspendMaintenance :${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                                   }

                               }else{

                                   suspendView.visibility = View.VISIBLE
                                   blur_view.visibility = View.VISIBLE

                                   vehicleModeCard.visibility = View.GONE
                                   mapDetailsCard.visibility = View.GONE
                                   refreshCard.visibility =View.GONE
                                   currentLocationCard.visibility = View.GONE
                                   layout_fabIcon.visibility = View.GONE

                                   suspendExpiredDialog.show(supportFragmentManager, "SuspendExpire")

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

                               suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")

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
                                       blur_view.visibility = View.VISIBLE

                                       vehicleModeCard.visibility = View.GONE
                                       mapDetailsCard.visibility = View.GONE
                                       refreshCard.visibility =View.GONE
                                       currentLocationCard.visibility = View.GONE
                                       layout_fabIcon.visibility = View.GONE


                                       suspendExpiredDialog.show(supportFragmentManager, "SuspendExpire")

                                       //  Toast.makeText(requireContext(), "SuspendExpire", Toast.LENGTH_SHORT).show()
                                   }

                               }else{

                                   suspendView.visibility = View.VISIBLE
                                   blur_view.visibility = View.VISIBLE

                                   vehicleModeCard.visibility = View.GONE
                                   mapDetailsCard.visibility = View.GONE
                                   refreshCard.visibility =View.GONE
                                   currentLocationCard.visibility = View.GONE
                                   layout_fabIcon.visibility = View.GONE

                                   suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")
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
                               blur_view.visibility = View.VISIBLE

                               vehicleModeCard.visibility = View.GONE
                               mapDetailsCard.visibility = View.GONE
                               refreshCard.visibility =View.GONE
                               currentLocationCard.visibility = View.GONE
                               layout_fabIcon.visibility = View.GONE
                               suspendDialog.show(supportFragmentManager, "SUSPEND_DIALOG")
                           }
                       }else{

                           //  customizeOptions()
                           val location = LatLng((it?.terminalDataLatitudeLast
                               ?: "0.0").toDouble(), (it?.terminalDataLongitudeLast ?: "0.0").toDouble())
                           mMapDrawer?.focusOnLocation(location)

                       }
                   }

               }catch (e:Exception){

               }
          }
      }
  }



    override fun onBackPressed() {
        //super.onBackPressed()

        mDialog = DialogHelper.getMessageDialog(this, "Alert",
            "Are you sure you want to Exit?")
            ?.positiveText("Yes")
            ?.cancelable(false)
            ?.onPositive { dialog, which ->
                dialog.dismiss()
                finish()
            }
            ?.negativeText("No")
            ?.onNegative { dialog, which ->
                dialog.dismiss()

            }
            ?.show()
    }

    fun getVehicleData() {
        if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
            setupVehicleObserver()

        } else {
            mVehicleViewModel.fetch(false)
            fetchCurrentLocation()
        }
    }

    private fun setupTopOptions(option: Int) {

        try {

            if (selectedOption == option)
                return

            val currentOption = selectedOption
            selectedOption = option

            if (selectedOption == OPTION_MONITOR) {
                updateOptionUI()

                if (currentOption == OPTION_LIST) {
                    mapFragment?.isShowMonitor = true
                    //  mapFragment?.customizeOptions()

                } else {
                    mapFragment = MapFragment.newInstance()
                    isFirstTime = true
                    commitFragmentTransaction(mapFragment, "Monitor")
                }

                return
            }

            if (selectedOption == OPTION_LIST) {
                updateOptionUI()

                if (currentOption == OPTION_MONITOR) {
                    mapFragment?.isShowMonitor = false
                    // mapFragment?.customizeOptions()
                    mapFragment?.showVehicleChooser()

                } else {
                    mapFragment = MapFragment.newInstance()
                    isFirstTime = true
                    mapFragment?.isShowMonitor = false
                    commitFragmentTransaction(mapFragment, "Monitor")
                }

                return
            }

            if (selectedOption == OPTION_NOTIFICATION) {
                updateOptionUI()
                commitFragmentTransaction(NotificationFragment(), "Notification")
                mapFragment = null
                return
            }

            if (selectedOption == OPTION_ANALYTICS) {
                updateOptionUI()
                commitFragmentTransaction(AnalyticsFragment(), "Analytics")
                mapFragment = null
                return
            }

        }catch (e:Exception){

        }

    }

    private fun updateOptionUI() {
        textView_liveMap.visibility = if (selectedOption == OPTION_MONITOR) View.VISIBLE else View.GONE
        textView_vehicles.visibility = if (selectedOption == OPTION_LIST) View.VISIBLE else View.GONE
        textView_notifications.visibility = if (selectedOption == OPTION_NOTIFICATION) View.VISIBLE else View.GONE
        textView_analytics.visibility = if (selectedOption == OPTION_ANALYTICS) View.VISIBLE else View.GONE

        fabMonitor.visibility = if (selectedOption == OPTION_MONITOR) View.GONE else View.VISIBLE
        fabVehicles.visibility = if (selectedOption == OPTION_LIST) View.GONE else View.VISIBLE
        layout_notifications.visibility = if (selectedOption == OPTION_NOTIFICATION) View.GONE else View.VISIBLE
        fabAnalytics.visibility = if (selectedOption == OPTION_ANALYTICS) View.GONE else View.VISIBLE
    }

    private fun commitFragmentTransaction(fragment: Fragment?, tag: String) {
        try {
            fragment?.let {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.containerFragment, it, tag)
                        .commit()
            }
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    private fun hideNotificationFabIfRequired() {
        when (userSource) {
            UserSource.VERSION_3 -> {
                layout_notifications.visibility = View.VISIBLE
                txtToolbarNotificationNumber.visibility = View.VISIBLE
            }
            UserSource.VERSION_2 -> {
                layout_notifications.visibility = View.GONE
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

    private val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        closeDrawer()

        var intent: Intent?
        when (item.itemId) {
            R.id.navigation_route -> {
                intent = Intent(this, VehicleRouteAnalyticsActivity::class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
            }
            R.id.navigation_account -> {
                openAccountPage()
            }
            R.id.navigation_expense -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_EXPENSE_LIST)
            }
            R.id.navigation_daily_distance -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_DISTANCE_REPORT)
            }
            R.id.navigation_daily_engine -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_ENGINE_ON_REPORT)
            }
            R.id.navigation_notification -> {
                val notificationFragment = NotificationFragment()
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, notificationFragment).commit()
            }
            R.id.navigation_pay_bill -> {
                BillingActivity.intent(this)
            }
            R.id.navigation_support -> {
                startActivity(Intent(this, HelpAndSupportActivity::class.java))
            }
            R.id.navigation_hourly_distance -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_HOURLY_DISTANCE_REPORT)
            }
            R.id.navigation_location_report -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_LOCATION_REPORT)
            }
            R.id.navigation_monthly_report -> {
                FragmentActivity.startActivity(this, FragmentActivity.TAG_MONTHLY_DISTANCE_REPORT)
            }
            R.id.navigation_trip_report -> {
                intent = Intent(this, TripReportActivity::class.java)
                intent.putExtra("cookie", mPrefRepository.cookie)
                startActivity(intent)
            }
            R.id.navigation_logout -> {

                mDialog = DialogHelper.getMessageDialog(this, "Confirm Logout",
                    "Are you sure you want to logout?")
                    ?.positiveText("Yes")
                    ?.cancelable(false)
                    ?.onPositive { dialog, which ->
                        dialog.dismiss()
                        //finish()
                        mFirebaseAnalyticsViewModel.logOut()
                        mProfileViewModel.logout {
                            VehicleTrackApplication.app?.startLogoutProcedure({
                                if (VehicleTrackApplication.app?.isCurrentlyLoginActivity == false) {

                                    intent = Intent(this, SplashScreenActivity::class.java)
                                    intent?.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent?.putExtra(SplashScreenActivity.EXTRA_REMOVE_DATA, true)
                                    startActivity(intent)
                                }
                            }, true)
                        }
                    }
                    ?.negativeText("No")
                    ?.onNegative { dialog, which ->
                        dialog.dismiss()

                    }
                    ?.show()



            }
            else -> {
                if (shouldGotoSwitchUserScreen(item))
                    gotoSwitchUserScreen(this)
            }
        }
        return@OnNavigationItemSelectedListener true
    }

    private fun openAccountPage() {
        intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    private fun closeDrawer() {
        container.closeDrawer(GravityCompat.START)
    }

    override fun fetchCurrentLocation() {
        mCurrentVehicleStatusLiveData = mVehicleViewModel.getCurrentVehicleStatus()

        mCurrentVehicleStatusLiveData?.observe(this, currentVehicleStatusObserver())
    }

    override fun fetchCurrentTravelledDistance() {
        mapFragment?.fetchCurrentTravelledDistance()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopTicker()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onResume() {
        super.onResume()

        setupDrawerHeader()

//        mVehicleViewModel.getVehicles()

        val shouldShowSwitchUserMenu = (mPrefRepository.user?.userGroupIdentifier.equals("ADMINISTRATOR", ignoreCase = true)
                && mPrefRepository.userSource == UserSource.VERSION_3.identifier)
        if(!shouldShowSwitchUserMenu) {
            hideSwitchUserMenuItem(navigationDrawerView.menu)
        }

        observerNotifications()
    }

    private fun setupDrawerHeader() {
        val headerView = navigationDrawerView.getHeaderView(0)
        headerView.setOnClickListener {
            openAccountPage()
            closeDrawer()
        }

        mProfileViewModel.fetchOrGetProfileInformation().observe(this, Observer {
            headerView.findViewById<TextView>(R.id.txtNvName).text = it.data?.name
                    ?: "Bondstein Technologies Ltd."
            headerView.findViewById<TextView>(R.id.txtNvEmail).text = it.data?.email
                    ?: Constants.VISIT_URL
        })
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        runOnUiThread {
            fetchCurrentLocation()
            fetchCurrentTravelledDistance()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                mapFragment?.requestToEnableGPS()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                mapFragment?.checkLocationPermission(this)
            }
        }
    }

    private fun setupVehicleObserver() {
        isSetObserver = true
        mVehicleViewModel.getVehicles().observe(this, Observer {
            if (it.isEmpty()) return@Observer

            terminals.clear()
            terminals.addAll(it)

            if (mPrefRepository.currentVehicle() == "" && terminals.size > 0) {

                val model = terminals[0]
                mPrefRepository.changeCurrentVehicle(model.bstid, model.vrn, model.bid, "${model.terminalDataLatitudeLast},${model.terminalDataLongitudeLast}")
            }
         ///  Toast.makeText(this, "Updating Location", Toast.LENGTH_SHORT).show()
            mapFragment?.updateVehicleList(it as ArrayList<Terminal>)
        })
    }

    override var mCurrentVehicleStatusLiveData: LiveData<Resource<VehicleStatus>>? = null

    override fun updateCurrentVehicleStatus(data: VehicleStatus?) {
        if (!isSetObserver)
         setupVehicleObserver()

        mVehicleViewModel.getCurrentVehicle { terminal ->
            terminal?.let {
                if(it.terminalAssignmentIsSuspended == "1"){
                    //show dialog


                }else{
                    //Toast.makeText(this, "Updating Location ${it.terminalAssignmentIsSuspended}", Toast.LENGTH_SHORT).show()
                    mapFragment?.updateCurrentVehicleStatus(data)
                }
            }
        }
    }

    override fun setProgressVisibility(visibility: Int) {
        mapFragment?.setProgressVisibility(visibility)
    }

    companion object {
        const val LOCATION_PERMISSION = 1
    }
}