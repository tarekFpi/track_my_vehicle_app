package com.singularity.trackmyvehicle.view.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.gaurav.gesto.OnGestureListener
import com.github.chuross.library.ExpandableLayout
import com.github.chuross.library.OnExpandListener
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.event.CurrentDateChangeEvent
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.view.customview.TimeSeekBar
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseCreatorFragmentCallback
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseListFragmentCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.vivekkaushik.datepicker.DatePickerTimeline
import com.vivekkaushik.datepicker.OnDateSelectedListener
import com.xw.repo.BubbleSeekBar
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.layout_current_vehicle.*
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import kotlinx.android.synthetic.main.layout_seekbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class VehicleRouteActivity : BaseActivity(), ExpenseListFragmentCallback, ExpenseCreatorFragmentCallback {

    private val LOCATION_REFRESH_INTERVAL: Long = 5000

    private fun showRoute() {
        routeButtonClicked()
        mAnalytics.routeViewed(mSelectedDateTime)
    }

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

    var terminalId: String = ""

    var cookie: String = ""

    val script: String = "API/V2/TerminalData/List"

    var liveData = MutableLiveData<Resource<List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>>>()

    private var mDialog: MaterialDialog? = null

    private var mCurrentVehicleRouteLiveData: List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>? = null


    /*private val mCurrentVehicleRouteObserver = Observer<List<VehicleRouteTerminal.VehicleRouteSuccessResponse.VehicleRouteTerminalData>> { data ->

        mMapDrawer?.drawVehicleRoute(data ?: ArrayList(), mSelectedDateTime)
        mMapDrawer?.placeFirstValue()
        if (data?.size != 0 && mMapDrawer?.isVehicleRouteDataAvailable() != false) {
            showSeekBar()
        }
    }*/

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

                mMapDrawer?.drawVehicleRoute(mCurrentVehicleRouteLiveData, mSelectedDateTime)
                mMapDrawer?.placeFirstValue()
                if (mCurrentVehicleRouteLiveData?.isNotEmpty() == true && mMapDrawer?.isVehicleRouteDataAvailable() != false) {
                    showSeekBar()
                }
            }
            Status.LOADING -> {
                Log.d("yjhfjhv","Entered 1")

                if (data.data?.vehicleRouteData?.size == 0) {
                    loadingSnackbar = Snackbar.make(imgClose.rootView, "Loading Routes...", Snackbar.LENGTH_INDEFINITE)
                    loadingSnackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.setTextColor(
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
        setContentView(R.layout.activity_vehicle_route)

        terminalId = mPrefRepository.currentVehicleTerminalId.toIntOrNull().toString()
        Log.d("igkjbk","$terminalId")

        mVehicleViewModel.fetch()
        hideSeekBar()?.setNow()
        onDateSelected(Date())

        mAnalytics.currentLocationViewed()

        imgChangeVehicle.setOnClickListener {
            val dialogFrag = BottomNavFragment.newInstance();
            dialogFrag.show(supportFragmentManager, dialogFrag.getTag())
        }

        fabCurrentLocation.setOnClickListener {
            mMapDrawer?.focusOnCurrentStatus()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (isGooglePlayServicesAvailable(this)) {
            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
            mapFragment.getMapAsync(mMapDrawer)
        }

        imgClose.setOnClickListener {
            this.finish()
        }

        val timeSeekBar = TimeSeekBar(seekbar)
        timeSeekBar.setup()

        seekbar.onProgressChangedListener = object :
                BubbleSeekBar.OnProgressChangedListenerAdapter() {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                           progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onChanged int:%d, float:%.1f", progress,
                        progressFloat)
                Log.d("TAG", s)

                mMapDrawer?.placeVehicleAt(progress)

                var route = mMapDrawer?.fetchVehicleRouteAt(progress)

                /*txtVehicleSpeed.text = (if (route?.speed.isNullOrBlank() == true) "--.--" else route?.speed) + " Km/Hr"
                txtEngineStatus.text = if (route?.engineStatus.isNullOrBlank()) "OFF" else route?.engineStatus
                txtVehicleSpeed.setTextColor(
                        if (route?.engineStatus?.toUpperCase() == "OFF") ContextCompat.getColor(
                                this@VehicleRouteActivity, R.color.colorTextDisable)
                        else ContextCompat.getColor(this@VehicleRouteActivity, R.color.colorTextPrimary)
                )

                if (txtEngineStatus.text == "OFF") {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_disabled)
                    imgEngineStatus.setImageDrawable(icon)
                } else if (txtEngineStatus.text == "ON" && (route?.speed?.toFloatOrNull() == 0f || route?.speed == null)) {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_default)
                    imgEngineStatus.setImageDrawable(icon)
                } else {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_active)
                    imgEngineStatus.setImageDrawable(icon)
                }*/

                val selectedTime = DateTime.now().withMillisOfDay(1000 * 60 * progress)
                txtSeekBarTime.text = selectedTime.toString("hh:mm a")

                if (route != null) {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteActivity, R.color.black))
                } else {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteActivity, R.color.colorTextSecondary))
                }
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                               progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onActionUp int:%d, float:%.1f", progress,
                        progressFloat)
                Log.d("TAG", s)
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int,
                                              progressFloat: Float) {
                val s = String.format(Locale.ENGLISH, "onFinally int:%d, float:%.1f", progress,
                        progressFloat)
                Log.d("TAG", s)

                mMapDrawer?.placeVehicleAt(progress, true)

                var route = mMapDrawer?.fetchVehicleRouteAt(progress)

                /*txtVehicleSpeed.text = (if (route?.speed.isNullOrBlank() == true) "--.--" else route?.speed) + " Km/Hr"
                txtEngineStatus.text = if (route?.engineStatus.isNullOrBlank()) "OFF" else route?.engineStatus
                txtVehicleSpeed.setTextColor(
                        if (route?.engineStatus?.toUpperCase() == "OFF") ContextCompat.getColor(
                                this@VehicleRouteActivity, R.color.colorTextDisable)
                        else ContextCompat.getColor(this@VehicleRouteActivity, R.color.colorTextPrimary)
                )
                if (txtEngineStatus.text == "OFF") {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_disabled)
                    imgEngineStatus.setImageDrawable(icon)
                } else if (txtEngineStatus.text == "ON" && (route?.speed?.toFloatOrNull() == 0f || route?.speed == null)) {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_default)
                    imgEngineStatus.setImageDrawable(icon)
                } else {
                    val icon = ContextCompat.getDrawable(this@VehicleRouteActivity,
                            R.drawable.ic_flash_active)
                    imgEngineStatus.setImageDrawable(icon)
                }*/

                val selectedTime = DateTime.now().withMillisOfDay(1000 * 60 * progress)
                txtSeekBarTime.text = selectedTime.toString("hh:mm a")

                if (route != null) {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteActivity, R.color.black))
                } else {
                    txtSeekBarTime.setTextColor(
                            ContextCompat.getColor(this@VehicleRouteActivity, R.color.colorTextSecondary))
                }
            }
        }

        /*containerDateAndMonth.setOnClickListener {
             elDatePicker.toggle()
         }
         */
        datePickerTimeline.configureForApp(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                val date = DateTime.now().withDate(year, month + 1, day)
                if (date.isAfter(DateTime.now().withHourOfDay(23).withMinuteOfHour(59))) {
                    Toasty.error(this@VehicleRouteActivity, "Select a Date Before Today").show()
                    return
                }

                onDateSelected(date.toDate())
                EventBus.getDefault().post(CurrentDateChangeEvent(DateTime(date)))
            }

            override fun onDisabledDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int,
                                                isDisabled: Boolean) {

            }
        })


        btnPlayStop.setOnClickListener {

            if (mMapDrawer?.isVehicleRouteDataAvailable() == false) {
                Log.d("uthfuyj","entered in btnp 1")
                return@setOnClickListener
            }
            if (isPlaying) {
                Log.d("uthfuyj","entered in btnp 2")
                stop()
                btnPlayStop.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_play_arrow))
                mAnalytics.routePlayStop(mSelectedDateTime, seekbar.progress)
                return@setOnClickListener
            }

            btnPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop))
            play()
            Log.d("uthfuyj","entered in btnp 3")
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
                        ContextCompat.getDrawable(this@VehicleRouteActivity, R.drawable.vd_down_chevron))
            }

            override fun onCollapsed(p0: ExpandableLayout?) {
                imgCollapse.setImageDrawable(
                        ContextCompat.getDrawable(this@VehicleRouteActivity, R.drawable.vd_up_chevron))
            }
        })

        showRoute()

        txtCurrentBstId.setOnTouchListener(object : OnGestureListener(this) {
            override fun onSwipeBottom() {
                mVehicleViewModel.selectNextVehicle()

            }

            override fun onSwipeTop() {
                mVehicleViewModel.selectPreviousVehicle()
            }
        })
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
                        ContextCompat.getDrawable(this@VehicleRouteActivity, R.drawable.ic_play_arrow))
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
                ContextCompat.getDrawable(this@VehicleRouteActivity, R.drawable.ic_play_arrow))
    }

    private fun getCurrentVehicleRoutes(date: Date) {
        stop()
        var vehicleRouteMode = "1"
        var terminalDataTimeFrom = "${DateTime(date).toString("yyyy-MM-dd")} 00:00:00"
        var terminalDataTimeTo = "${DateTime(date).toString("yyyy-MM-dd")} 23:59:59"
        terminalId

        mCurrentVehicleLiveData?.removeObserver(mCurrentVehicleObserver)
//        mCurrentVehicleRouteLiveData?.observe(this, mCurrentVehicleRouteObserver)

        /* mCurrentVehicleRouteLiveData = mVehicleViewModel.getCurrentVehicleRoutes(
                DateTime(date).toString("yyyy-MM-dd"))*/
        //mCurrentVehicleRouteLiveData = getVehicleRouteResponse(DateTime(date).toString("yyyy-MM-dd"))
        showLoading()
        mCurrentVehicleLiveData = mVehicleViewModel.getCurrentRouteVehicle(
                script,  vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
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


        txtDate.text = mSelectedDateTime?.toString("d")
        txtMonth.text = mSelectedDateTime?.toString("MMM")?.toUpperCase()
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

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
//        mCurrentStatusHandler.post(mCurrentStatusRunnable)
    }

    override fun addExpenseClicked() {
        TODO("Not yet implemented")
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
            //            updateCurrentLocation()
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
        txtCurrentBstId.text = if (currentVehicle.isEmpty()) "Loading..." else currentVehicle
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


}

fun DatePickerTimeline.configureForApp(listener: OnDateSelectedListener) {
    val today = DateTime.now()
    val firstDay = DateTime.now().minusMonths(3)
    this.setInitialDate(firstDay.year, firstDay.monthOfYear - 1, firstDay.dayOfMonth)
    this.setActiveDate(today.toCalendar(Locale.getDefault()))
    this.setOnDateSelectedListener(listener)

}
