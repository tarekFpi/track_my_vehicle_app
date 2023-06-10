package com.singularity.trackmyvehicle.view.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.SpeedViolationAdapter
import com.singularity.trackmyvehicle.view.customview.BarChartHandler
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.twinkle94.monthyearpicker.picker.YearMonthPickerDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_reports.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import kotlin.collections.ArrayList

@Deprecated("ReportsActivity Changed to ReportTagActivity")
class ReportsActivity : BaseActivity() {
    @Inject
    lateinit var mReportViewModel: ReportsViewModel
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    private var mSelectedDateTime: DateTime? = DateTime.now()

    private var STATE: REPORT_STATE = REPORT_STATE.SPEED

    private val mSpeedViolationAdapter = SpeedViolationAdapter()

    private val mDistanceReportObserver = Observer<Resource<List<DistanceReport>>> { data ->
        if (data?.data != null && data.data.isNotEmpty()) {
            val values = data.data
            val dates: ArrayList<DateTime> = ArrayList()
            val floats: ArrayList<Float> = ArrayList()
            values.forEach { value ->
                dates.add(DateTime.parse(value.date, DateTimeFormat.forPattern("yyyy-MM-dd")))
                floats.add(value.km.toFloatOrNull() ?: 0F)
            }

            BarChartHandler(barChartDistance,
                    "Date: ",
                    "Distance: ",
                    "",
                    dates,
                    floats
            )
            if (STATE == REPORT_STATE.DISTANCE) {
                Toasty.success(this, "Distance Report Loaded").show()
            }
        } else {
            if (data?.status == Status.LOADING && STATE == REPORT_STATE.DISTANCE) {
                Toasty.normal(this, "Distance Report Loading").show()
            }
        }
    }

    private val mSpeedAlertObserver = Observer<Resource<List<SpeedViolationModel>>> { data ->
        if (data?.data != null) {
            val values = data?.data
            val dates: ArrayList<DateTime> = ArrayList()
            val floats: ArrayList<Float> = ArrayList()
            val now = mSelectedDateTime ?: DateTime.now()
            for (i in 1 .. 31) {
                try {
                    floats.add(0.0f)

                    val date = now.withDayOfMonth(i)
                    dates.add(date)
                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    break
                }
            }
            values.forEach { value ->
                floats.set(DateTime(value.date).dayOfMonth, value.violations.toFloat())
            }

            BarChartHandler(barChartSpeedAlert,
                    "Date: ",
                    "Speed Alert: ",
                    "",
                    dates,
                    floats
            )
            if (STATE == REPORT_STATE.SPEED) {
                Toasty.success(this, "Speed Violation Report Loaded").show()
            }
            return@Observer
        }

        if (data?.status == Status.LOADING && STATE == REPORT_STATE.SPEED) {
            Toasty.normal(this, "Speed Violation Report Loading").show()
        }
    }

    private var mDistanceReportLiveData: MutableLiveData<Resource<List<DistanceReport>>>? = null

    private var mSpeedReportLiveData: MutableLiveData<Resource<List<SpeedViolationModel>>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)
        toggleReportState()
        updateDistanceReport()
        updateDateLabels()
        updateHeader()
        containerVehicleInfo.setOnClickListener {
            showVehicleSelectFragment()
        }
        imgSearch.setOnClickListener {
            showVehicleSelectFragment()
        }
        txtDate.setOnClickListener {
            val yearMonthPickerDialog = YearMonthPickerDialog(this, YearMonthPickerDialog.OnDateSetListener { year, month ->
                mSelectedDateTime = DateTime.now().withMonthOfYear(month + 1)
                        .withYear(year)
                updateDateLabels()

                when (STATE) {
                    REPORT_STATE.DISTANCE -> updateDistanceReport()
                    REPORT_STATE.SPEED    -> updateSpeedReport()
                }
            })
            yearMonthPickerDialog.show();

        }
        speedReportList.layoutManager = LinearLayoutManager(this)

        speedReportList.adapter = mSpeedViolationAdapter
        txtReportType.setOnClickListener {
            toggleReportState()
        }


    }

    private fun toggleReportState() {

        when (STATE) {
            REPORT_STATE.DISTANCE -> {
                txtReportType.text = "SPEED REPORT"
                updateSpeedReport()
                barChartSpeedAlert.visibility = View.GONE
                speedList.visibility = View.VISIBLE
                speedReportList.visibility = View.GONE
                speedEmptyList.visibility = View.VISIBLE
                barChartDistance.visibility = View.GONE
                STATE = REPORT_STATE.SPEED
            }
            REPORT_STATE.SPEED    -> {
                txtReportType.text = "DISTANCE REPORT"
                updateDistanceReport()
                barChartSpeedAlert.visibility = View.GONE
                speedList.visibility = View.GONE
                speedReportList.visibility = View.GONE
                speedEmptyList.visibility = View.GONE
                barChartDistance.visibility = View.VISIBLE
                STATE = REPORT_STATE.DISTANCE
            }
        }
    }

    private var mSpeedViolatointLiveData: MutableLiveData<Resource<List<SpeedAlertReport>>>? = null
    private val mSpeedViolationObserver = Observer<Resource<List<SpeedAlertReport>>> { data ->
        if (STATE == REPORT_STATE.SPEED) {
            speedList.visibility - View.VISIBLE
            val listData: List<SpeedAlertReport> = data?.data ?: ArrayList()
            speedEmptyList.visibility = if (listData.isEmpty())
                View.VISIBLE
            else
                View.GONE
            speedReportList.visibility = if (listData.isNotEmpty())
                View.VISIBLE
            else
                View.GONE
            mSpeedViolationAdapter.setItems(listData.toMutableList())

            when (data?.status) {
                Status.SUCCESS -> {
                    Toasty.success(this@ReportsActivity, "Speed report loaded").show()
                }
                Status.ERROR   -> {
                    Toasty.error(this@ReportsActivity, "Speed report loading failed").show()

                }
                Status.LOADING -> {
                    Toasty.normal(this@ReportsActivity, "Speed report Loading").show()
                }
            }

        }
    }

    private fun updateSpeedReport() {
        /*
        mSpeedReportLiveData?.removeObserver(mSpeedAlertObserver)
        mSpeedReportLiveData = mReportViewModel.fetchCurrentVehicleSpeedReport(mSelectedDateTime
                ?: DateTime.now())
        mSpeedReportLiveData?.observe(this, mSpeedAlertObserver)
        */
        mSpeedViolatointLiveData?.removeObserver(mSpeedViolationObserver)
        mSpeedViolatointLiveData = mReportViewModel.fetchCurrentVehicleSpeedViolatoint(mSelectedDateTime
                ?: DateTime.now())

        mSpeedViolatointLiveData?.observe(this, mSpeedViolationObserver)


    }

    private fun updateDateLabels() {
        txtDate.setText(mSelectedDateTime?.toString("MMMM yyyy"))

    }

    private fun updateDistanceReport() {
        mDistanceReportLiveData?.removeObserver(mDistanceReportObserver)
        mDistanceReportLiveData = mReportViewModel.fetchCurrentVehicleDistanceReport(mSelectedDateTime
                ?: DateTime.now())
        mDistanceReportLiveData?.observe(this, mDistanceReportObserver)

    }

    companion object {
        fun intent(context: Context) {
            context.startActivity(Intent(context, ReportsActivity::class.java))
        }
    }

    private fun showVehicleSelectFragment() {
        val dialogFrag = BottomNavFragment.newInstance();
        dialogFrag.show(supportFragmentManager, dialogFrag.getTag())
    }


    private fun updateHeader() {
        txtVehicleName.text = mVehicleViewModel.mPrefRepository.currentVehicle()
        txtVehicleTitle.text = mVehicleViewModel.mPrefRepository.currentVehicleVrn()
    }

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        updateHeader()
        when (STATE) {
            REPORT_STATE.DISTANCE -> updateDistanceReport()
            REPORT_STATE.SPEED    -> updateSpeedReport()
        }

    }

    override fun onResume() {
        super.onResume()
        if (! EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

    }

    enum class REPORT_STATE {
        SPEED,
        DISTANCE
    }


}

