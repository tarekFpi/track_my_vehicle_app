package com.singularity.trackmyvehicle.view.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.github.chuross.library.ExpandableLayout
import com.github.chuross.library.OnExpandListener
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.internal.it
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.databinding.ActivityAnalyticsVehicleRouteBinding
import com.singularity.trackmyvehicle.model.apiResponse.v3.EventsVehicleRouteAnalyticsItem
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.utils.dialog.CustomInfoDialogVehicleRouteAnalytics
import com.singularity.trackmyvehicle.view.adapter.EventsVehicleRouteAnalyticsAdapter
import com.singularity.trackmyvehicle.view.adapter.MotionStateVehicleRouteAnalyticsAdapter
import com.singularity.trackmyvehicle.view.customview.TimeSeekBar
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendExpiredDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendedMaintenanceDialog
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.view.fragment.EventListFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.EventListCallback
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseCreatorFragmentCallback
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseListFragmentCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehicleRouteAnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.xw.repo.BubbleSeekBar
import dagger.android.AndroidInjection
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_analytics_vehicle_route.*
import kotlinx.android.synthetic.main.item_events_vehicle_route_analytics.*
import kotlinx.android.synthetic.main.layout_add_events_vehicle_route_analytics.*
import kotlinx.android.synthetic.main.layout_current_vehicle.*
import kotlinx.android.synthetic.main.layout_seekbar.*
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.*
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.btnBack
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.btnNext
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.btnPlayStop
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.containerSeekbar
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.exlSeedbar
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.imgCollapse
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.seekbar
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.txtSeekBarTime
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class VehicleRouteAnalyticsActivity : BaseActivity(), ExpenseListFragmentCallback, ExpenseCreatorFragmentCallback{

    var customInfoDialogVehicleRouteAnalytics : CustomInfoDialogVehicleRouteAnalytics = CustomInfoDialogVehicleRouteAnalytics()

    private lateinit var vehicleRouteAnalyticsViewModel : VehicleRouteAnalyticsViewModel

    var cookie : String = ""

    var vehicleRouteMode : String = ""

    var terminalDataTimeFrom : String = ""

    var terminalDataTimeTo : String = ""
    private var startPoint = 0

    var checkedEvent : Boolean = false

    private var eventList : ArrayList<EventsVehicleRouteAnalyticsItem> = ArrayList()

    private fun showRoute() {
        routeButtonClicked()
        mAnalytics.routeViewed(mSelectedDateTime)
    }

    val suspendDialog = SuspendDialog()

    private var mMapDrawer: MapDrawer? = null

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mPrefRepository: PrefRepository

    @Inject
    lateinit var mVehicleRepository: VehicleRepository

    @Inject
    lateinit var mAnalytics: AnalyticsViewModel

    private var mSelectedDateTime: DateTime? = null

    private var loadingSnackbar: Snackbar? = null


    val suspendExpiredDialog = SuspendExpiredDialog()

    val suspendMaintenanceDialog = SuspendedMaintenanceDialog()


    private lateinit var appPreference : AppPreference

    private lateinit var relativeLayoutMap: RelativeLayout
    lateinit var suspendView: RelativeLayout
    lateinit var blurView: ImageView

    var terminalId: String = ""

    var liveData = MutableLiveData<Resource<List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>>>()

    private var mDialog: MaterialDialog? = null

    private var mCurrentVehicleRouteLiveData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>? = null
    private var mCurrentVehicleMotionStateData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>? = null
    private var mCurrentVehicleRouteEventLiveData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteEvent>? = null
    private var mCurrentVehicleLocationData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteGeoLocationPosition> = ArrayList()

    private var mCurrentVehicleLiveData: LiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>>? = null
    private val mCurrentVehicleObserver = Observer<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>> { data ->
        mMapDrawer?.setTraffic(false)
        dismissDialog()

        if (data == null)
            return@Observer

        if (data.status != Status.LOADING) {
            loadingSnackbar?.dismiss()
        }

        mMapDrawer?.removePolyLine()

        val TOAST_DURATION = 200
        when (data.status) {
            Status.SUCCESS -> {

                if (mMapDrawer?.isVehicleRouteDataAvailable() == false) {
                    Toasty.success(this, "Route loaded", TOAST_DURATION).show()
                }


                mCurrentVehicleRouteLiveData = data.data?.vehicleRouteData ?: ArrayList()
                mCurrentVehicleMotionStateData = data.data?.vehicleRouteMotionState ?: ArrayList()
                mCurrentVehicleRouteEventLiveData = data.data?.vehicleRouteEvent ?: ArrayList()
                mCurrentVehicleLocationData = data.data?.vehicleRouteGeoLocationPosition ?: ArrayList()

                startPoint = 0
                for (item in mCurrentVehicleRouteLiveData ?: ArrayList()) {
                    if ((item.terminalDataVelocity?.toFloat() ?: 0.0F) > 0.0F) {
                        val updateTime = item.convertTimeToDateFormat()
                        val minute = (updateTime.hours * 60) + updateTime.minutes
                        startPoint = minute

                        break
                    }
                }

                mMapDrawer?.setLocationList(mCurrentVehicleLocationData)

                loadVehicleMotionState(data.data?.vehicleRouteMotionState as ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>?
                        ?: ArrayList())

                mMapDrawer?.drawVehicleRoute(mCurrentVehicleRouteLiveData, mSelectedDateTime)
                mMapDrawer?.setMarkers(mCurrentVehicleRouteEventLiveData ?: ArrayList())

                mMapDrawer?.setPolyLineColor(mCurrentVehicleMotionStateData)
                //mMapDrawer?.drawParkingRoute(mCurrentVehicleRouteParkingLiveData, mSelectedDateTime)

                mMapDrawer?.placeFirstValue()
                if (mCurrentVehicleRouteLiveData?.isNotEmpty() == true && mMapDrawer?.isVehicleRouteDataAvailable() != false) {
                    showSeekBar()
                }
            }
            Status.LOADING -> {

                if (data.data?.vehicleRouteData?.size == 0) {
                    loadingSnackbar = Snackbar.make(imgClose.rootView, "Loading Routes...", Snackbar.LENGTH_INDEFINITE)
                    loadingSnackbar?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(
                            Color.WHITE)
                    loadingSnackbar?.show()
                } else {
                    loadingSnackbar?.dismiss()
                }
            }
            Status.ERROR -> {
                if (data.message?.contentEquals("Vehicle not moved, no route found.") == true) {
                    Toasty.warning(this, "Vehicle had not moved. No route found.", TOAST_DURATION)
                            .show()
                    return@Observer
                }
                Toasty.error(this, data.message ?: "Error Loading Routes", TOAST_DURATION).show()
            }
        }

        if (data.data?.vehicleRouteData?.size != 0 && mMapDrawer?.isVehicleRouteDataAvailable() != false) {
            showSeekBar()
        }

        /*mMapDrawer?.drawRoute(data.data, mSelectedDateTime)
        mMapDrawer?.placeFirstValue()
        if (data.data?.vehicleRouteTerminalData?.size != 0 && mMapDrawer?.isRouteDataAvailable() != false) {
            showSeekBar()
        }*/
    }

    private fun showSeekBar() {
        ExpectAnim()
                .expect(containerSeekbar)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_analytics_vehicle_route)

        vehicleRouteAnalyticsViewModel = ViewModelProvider(this).get(VehicleRouteAnalyticsViewModel::class.java)

        val binding = DataBindingUtil.setContentView<ActivityAnalyticsVehicleRouteBinding>(this, R.layout.activity_analytics_vehicle_route)

        relativeLayoutMap=findViewById(R.id.container)

        suspendView =findViewById<RelativeLayout>(R.id.suspendView)
        blurView = findViewById<ImageView>(R.id.blur_view)

        binding.lifecycleOwner = this
        binding.eventListViewModel = vehicleRouteAnalyticsViewModel
        AndroidInjection.inject(this)

        terminalId = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()

        mVehicleViewModel.fetch()
        hideSeekBar()?.setNow()
        onDateSelected(Date())

        mAnalytics.currentLocationViewed()

        textView_bstId.setOnClickListener {
            val dialogFrag = BottomNavFragment.newInstance();
            dialogFrag.show(supportFragmentManager, dialogFrag.getTag())
        }




        val timeSeekBar = TimeSeekBar(seekbar)
        timeSeekBar.setup()

        seekbar.onProgressChangedListener = object :
                BubbleSeekBar.OnProgressChangedListenerAdapter() {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                           progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onChanged int:%d, float:%.1f", progress,
                        progressFloat)

                mMapDrawer?.placeVehicleAt(progress)

                var route = mMapDrawer?.fetchVehicleRouteAt(progress)

                val selectedTime = DateTime.now().withMillisOfDay(1000 * 60 * progress)
                txtSeekBarTime.text = selectedTime.toString("hh:mm a")

                if (route != null) {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteAnalyticsActivity, R.color.black))
                } else {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteAnalyticsActivity, R.color.colorTextSecondary))
                }
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                               progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onActionUp int:%d, float:%.1f", progress,
                        progressFloat)
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                              progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onFinally int:%d, float:%.1f", progress,
                        progressFloat)

                mMapDrawer?.placeVehicleAt(progress, true)

                var route = mMapDrawer?.fetchVehicleRouteAt(progress)

                val selectedTime = DateTime.now().withMillisOfDay(1000 * 60 * progress)
                txtSeekBarTime.text = selectedTime.toString("hh:mm a")

                if (route != null) {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteAnalyticsActivity, R.color.black))
                } else {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteAnalyticsActivity, R.color.colorTextSecondary))
                }
            }
        }

        layout_date.setOnClickListener {
            // calender class's instance and get current date , month and year from calender
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR] // current year

            val mMonth = c[Calendar.MONTH] // current month

            val mDay = c[Calendar.DAY_OF_MONTH] // current day

            // date picker dialog
           var datePickerDialog = DatePickerDialog(this@VehicleRouteAnalyticsActivity,
                   { view, year, monthOfYear, dayOfMonth ->
                       textView_day.text = dayOfMonth.toString()
                       textView_month.text = monthOfYear.toString()

                       val date = DateTime.now().withDate(year, monthOfYear + 1, dayOfMonth)

                       onDateSelected(date.toDate())

                   }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()

        }

        btnPlayStop.setOnClickListener {

            if (mMapDrawer?.isVehicleRouteDataAvailable() == false) {
                return@setOnClickListener
            }
            if (isPlaying) {
                stop()
                btnPlayStop.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_play_arrow))
                mAnalytics.routePlayStop(mSelectedDateTime, seekbar.progress)
                return@setOnClickListener
            }

            if (seekbar.progress < startPoint) {
                Log.d("uyho","$startPoint 2")
                seekbar.setProgress(startPoint.toFloat())
            }

            btnPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop))
            play()

            mAnalytics.routePlayStarted(mSelectedDateTime, seekbar.progress)
        }
        btnBack.setOnClickListener {
            if (mMapDrawer?.isVehicleRouteDataAvailable() == false) {
                return@setOnClickListener
            }
            val progress = seekbar.progress

            seekbar.setProgress(if (progress < 5) {
                0f
            } else {
                (progress - 5).toFloat()
            })
        }

        btnNext.setOnClickListener {

            if (mMapDrawer?.isVehicleRouteDataAvailable() == false) {
                return@setOnClickListener
            }

            val progress = seekbar.progress

            seekbar.setProgress(if (progress > 1434) {
                1434f
            } else {
                (progress + 5).toFloat()
            })
        }

        updateVehicleText()

        imgCollapse.setImageDrawable(ContextCompat.getDrawable(this,
                if (exlSeedbar.isCollapsed) R.drawable.vd_up_chevron else R.drawable.vd_down_chevron))
        imgCollapse.setOnClickListener {
            exlSeedbar.toggle()
        }

        exlSeedbar.setOnExpandListener(object : OnExpandListener {
            override fun onExpanded(p0: ExpandableLayout?) {
                imgCollapse.setImageDrawable(
                        ContextCompat.getDrawable(this@VehicleRouteAnalyticsActivity, R.drawable.vd_down_chevron))
            }

            override fun onCollapsed(p0: ExpandableLayout?) {
                imgCollapse.setImageDrawable(
                        ContextCompat.getDrawable(this@VehicleRouteAnalyticsActivity, R.drawable.vd_up_chevron))
            }
        })

        showRoute()

        visibilityHandle()

        backPressed()

        showInfo()

        setVehicleRouteFetchParameterData()

        setAddEvents()

        setdayNightMapStyle()

    }


    var isPlaying = false
    val mPlayHandler = Handler()

    private val mPlayRunnable = object : Runnable {
        override fun run() {
            val index = seekbar.progress + 1
            var maxIndex = 1440
            if (net.danlew.android.joda.DateUtils.isToday(mSelectedDateTime)) {
                maxIndex = DateTime.now().minuteOfDay().get()
            }
            if (index < maxIndex) {
                mPlayHandler.postDelayed(this, 200)
                seekbar.setProgress(index.toFloat())
            } else {
                isPlaying = false
                btnPlayStop.setImageDrawable(
                        ContextCompat.getDrawable(this@VehicleRouteAnalyticsActivity, R.drawable.ic_play_arrow))
            }
        }
    }

    private fun play() {
        mMapDrawer?.placeFirstValue()
        mPlayHandler.post(mPlayRunnable)
        isPlaying = true
    }

    private fun stop() {
        mPlayHandler.removeCallbacks(mPlayRunnable)
        isPlaying = false
        btnPlayStop.setImageDrawable(
                ContextCompat.getDrawable(this@VehicleRouteAnalyticsActivity, R.drawable.ic_play_arrow))
    }

    private fun getCurrentVehicleRoutes(date: Date) {
        stop()
        vehicleRouteMode = "1"
        terminalDataTimeFrom = "${DateTime(date).toString("yyyy-MM-dd")} 00:00:00"
        terminalDataTimeTo = "${DateTime(date).toString("yyyy-MM-dd")} 23:59:59"
        terminalId

        mCurrentVehicleLiveData?.removeObserver(mCurrentVehicleObserver)
//        mCurrentVehicleRouteLiveData?.observe(this, mCurrentVehicleRouteObserver)

        /* mCurrentVehicleRouteLiveData = mVehicleViewModel.getCurrentVehicleRoutes(
                DateTime(date).toString("yyyy-MM-dd"))*/
        //mCurrentVehicleRouteLiveData = getVehicleRouteResponse(DateTime(date).toString("yyyy-MM-dd"))
        showLoading()
        mCurrentVehicleLiveData = mVehicleViewModel.getCurrentRouteVehicle(
                ENDPOINTS.FETCH_ROUTE_VEHICLE, vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
        mCurrentVehicleLiveData?.observe(this, mCurrentVehicleObserver)
    }

    private fun routeButtonClicked() {
        mMapDrawer?.setTraffic(false)
        // getCurrentVehicleRoutes(mSelectedDateTime?.toDate() ?: Date())
        if (mMapDrawer?.isVehicleRouteDataAvailable() == true) {
            showSeekBar()
        }
    }

    private fun onDateSelected(date: Date) {
        mSelectedDateTime = DateTime(date)
        mAnalytics.routeViewed(mSelectedDateTime)


        textView_day.text = mSelectedDateTime?.toString("d")
        textView_month.text = mSelectedDateTime?.toString("MMM")?.toUpperCase()
//        elDatePicker.collapse()
        seekbar.setProgress(0f)
        txtSeekBarTime.text = "12:00 AM"

        getCurrentVehicleRoutes(mSelectedDateTime?.toDate() ?: Date())
    }

    private fun hideSeekBar(): ExpectAnim? {
        return ExpectAnim()
                .expect(containerSeekbar)
                .toBe(
                        Expectations.atItsOriginalPosition()
//                        outOfScreen(Gravity.BOTTOM)
                )
                .toAnimation()
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        mMapDrawer?.clearMap()

        appPreference = AppPreferenceImpl(applicationContext)
        //catch suspend method
            mVehicleViewModel.getCurrentVehicle {

                if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.robi.robitrackervts")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast == ""  || TerminalTimeLast == null){

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            layout_view_vehicle_route_analytics.visibility=View.GONE

                           /// suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")

                        }else  {

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

                                    layout_view_vehicle_route_analytics.visibility=View.GONE

                                 //   suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")
                                }

                            }else{
                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                layout_view_vehicle_route_analytics.visibility=View.GONE

                              //  suspendExpiredDialog.show(supportFragmentManager, "SuspendExpire")
                            }
                        }

                    }else{

                        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        if (isGooglePlayServicesAvailable(this)) {
                            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
                            mapFragment.getMapAsync(mMapDrawer)
                        }
                    }
                }else if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.ral.trackmyvehicle")){

                    if(it?.terminalAssignmentIsSuspended == "1"){

                        var TerminalTimeLast = it?.terminalDataTimeLast.toString()

                        if(TerminalTimeLast.equals("")  || TerminalTimeLast.equals("null")){

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                            layout_view_vehicle_route_analytics.visibility=View.GONE

                          //  suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")

                        }else  {

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

                                    layout_view_vehicle_route_analytics.visibility=View.GONE

                                   // suspendMaintenanceDialog.show(supportFragmentManager, "SuspendMaintenance")
                                }
                            }else{

                                suspendView.visibility = View.VISIBLE
                                blurView.visibility = View.VISIBLE

                                layout_view_vehicle_route_analytics.visibility=View.GONE

                                //suspendExpiredDialog.show(supportFragmentManager, "SuspendExpire")
                            }
                        }

                    }else{


                        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        if (isGooglePlayServicesAvailable(this)) {
                            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
                            mapFragment.getMapAsync(mMapDrawer)
                        }

                    }

                }else{


                    //================ tmv apps Suspended Condition================//

                    if(it?.terminalAssignmentIsSuspended == "1"){


                        if(!suspendDialog.shown){


                            layout_view_vehicle_route_analytics.visibility=View.GONE

                            appPreference.SetBstId(AppPreference.bstId,it?.bstId.toString())

                            suspendView.visibility = View.VISIBLE
                            blurView.visibility = View.VISIBLE

                           // suspendDialog.show(supportFragmentManager, "SUSPEND_DIALOG")
                        }
                    }else{

                        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        if (isGooglePlayServicesAvailable(this)) {
                            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
                            mapFragment.getMapAsync(mMapDrawer)
                        }
                    }
                }


            }
//        mCurrentStatusHandler.post(mCurrentStatusRunnable)
    }

    override fun addExpenseClicked() {

    }

    override fun expenseAdded() {
        supportFragmentManager.popBackStack()
    }

    override fun getSelectedDate(): DateTime {
        return mSelectedDateTime ?: DateTime.now()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        runOnUiThread {
            //  updateCurrentLocation()
            updateVehicleText()
            getCurrentVehicleRoutes(mSelectedDateTime?.toDate() ?: Date())
            /*txtVehicleSpeed.text = "--.-- Km/Hr"
            txtEngineStatus.text = "---"
            txtVehicleSpeed.setTextColor(
                    ContextCompat.getColor(this@VehicleRouteActivity, R.color.colorTextDisable))*/
            mMapDrawer?.removePolyLine()
            mMapDrawer?.clearMap()
        }


    }

    private fun updateVehicleText() {
        //  Log.e("hgfvu","Enter")
        val currentVehicle = mVehicleViewModel.mPrefRepository.currentVehicle()
        val currentVehicleVrn = mVehicleViewModel.mPrefRepository.currentVehicleVrn()

        textView_bstId.text = if (currentVehicle.isEmpty()) "Loading..." else currentVehicle.trim()
        textView_carRegistrationNumber.text = if (currentVehicleVrn.isEmpty()) "Loading..." else currentVehicleVrn.trim()

        textView_upperView_bstid.text = if (currentVehicle.isEmpty()) "Loading..." else currentVehicle.trim()
        textView_upperView_registrationNumber.text = if (currentVehicleVrn.isEmpty()) "Loading..." else currentVehicleVrn.trim()

        terminalId = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()

    }


    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance();
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404)?.show();
            }
            return false;
        }
        return true;
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

    private fun setVehicleRouteFetchParameterData() {

        cookie = intent.getStringExtra("cookie").toString()
        vehicleRouteAnalyticsViewModel.getVehicleRouteFetchParameterData(this, cookie, vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
    }

    private fun loadVehicleEvents() {
        vehicleRouteAnalyticsViewModel.loadEventsListData(this).observe(
                this, object : Observer<ArrayList<EventsVehicleRouteAnalyticsItem>> {
            override fun onChanged(data: ArrayList<EventsVehicleRouteAnalyticsItem>) {
                eventList = data

            }
        })
    }

    private fun setAddEvents() {
        loadVehicleEvents()

    }

    private fun setEvents() {

        mMapDrawer?.drawVehicleRoute(mCurrentVehicleRouteLiveData, mSelectedDateTime)

        for(item in eventList){
            if (item.itemNumber == 1) {
                item.checked?.let { mMapDrawer?.toggleParkingFlagMarker(it) }

            } else if (item.itemNumber == 2) {
                item.checked?.let { mMapDrawer?.toggleSuddenFlagMarker(it) }

            } else if (item.itemNumber == 3) {
                item.checked?.let { mMapDrawer?.toggleHarshFlagMarker(it) }

            } else if (item.itemNumber == 4) {
                item.checked?.let { mMapDrawer?.toggleViolationFlagMarker(it) }

            } else if (item.itemNumber == 5) {
                item.checked?.let { mMapDrawer?.toggleEngineFlagMarker(it) }

            } else if (item.itemNumber == 6) {
                item.checked?.let { mMapDrawer?.toggleIdleFlagMarker(it) }

            } else if (item.itemNumber == 7) {
                item.checked?.let { mMapDrawer?.togglePowerFlagMarker(it) }

            }
        }
    }

    var isNightMode : Boolean = false
    private fun setdayNightMapStyle() {

        cardView_nightMode.setOnClickListener {
            isNightMode = true
            cardView_nightMode.setCardBackgroundColor(resources.getColor(R.color.analyticsVehicleRouteIconColor))
            cardView_dayMode.setCardBackgroundColor(resources.getColor(R.color.analyticsVehicleRouteNightModeBgColor))
            mMapDrawer?.setNightMode(isNightMode)
        }

        cardView_dayMode.setOnClickListener {
            isNightMode = false
            cardView_nightMode.setCardBackgroundColor(resources.getColor(R.color.analyticsVehicleRouteNightModeBgColor))
            cardView_dayMode.setCardBackgroundColor(resources.getColor(R.color.analyticsVehicleRouteIconColor))
            mMapDrawer?.setNightMode(isNightMode)
        }
    }

    private fun loadVehicleMotionState(motionStateList: ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>) {
        if(motionStateList.isNullOrEmpty()){
            layout_aboveSeekbarDesign.visibility = View.GONE
        }
        val adapter = MotionStateVehicleRouteAnalyticsAdapter(this, motionStateList)
        recyclerView_motionState.isNestedScrollingEnabled = false
        recyclerView_motionState.setHasFixedSize(true)
        recyclerView_motionState.adapter = adapter
    }

    private fun showInfo() {
        cardView_info.setOnClickListener {
            customInfoDialogVehicleRouteAnalytics.showDialog(this)
        }
    }

    private fun backPressed() {
        imageView_crossButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun visibilityHandle() {
        cardView_routeAnalytics.setOnClickListener {
            cardView_routeAnalytics.visibility = View.GONE
            imageView_crossButton.visibility = View.GONE
            layout_selectedDateEvents.visibility = View.GONE

            layout_eventsItem.visibility = View.VISIBLE
            imageView_backButton.visibility = View.VISIBLE
            cardView_routeAnalytics.visibility = View.VISIBLE
            cardView_upperView.visibility = View.VISIBLE
            imageView_cancel.visibility = View.VISIBLE
            layout_eventItem.visibility = View.VISIBLE

        }

        imageView_backButton.setOnClickListener {
            cardView_routeAnalytics.visibility = View.VISIBLE
            imageView_crossButton.visibility = View.VISIBLE
            layout_selectedDateEvents.visibility = View.VISIBLE
            cardView_routeAnalytics.visibility = View.VISIBLE

            layout_eventsItem.visibility = View.GONE
            imageView_backButton.visibility = View.GONE
            cardView_upperView.visibility = View.GONE
        }

        imageView_cancel.setOnClickListener {
            imageView_cancel.visibility = View.GONE
            layout_eventItem.visibility = View.INVISIBLE
        }

        imageView_events.setOnClickListener {
            addEventsLayoutShowHide()
        }

        cardView_event.setOnClickListener{
            addEventsLayoutShowHide()
        }

    }

    private fun addEventsLayoutShowHide() {
        EventListFragment.newInstance(Gson().toJson(eventList), eventListCallback).show(supportFragmentManager, EventListFragment.TAG)
    }

    private val eventListCallback = object : EventListCallback {
        override fun onEventListFound(list: ArrayList<EventsVehicleRouteAnalyticsItem>) {
            eventList = list

            setEvents()
        }
    }
}