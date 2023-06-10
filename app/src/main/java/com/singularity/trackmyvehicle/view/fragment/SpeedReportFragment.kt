package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.gaurav.gesto.OnGestureListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.utils.BackGroundTasker
import com.singularity.trackmyvehicle.view.activity.configureForApp
import com.singularity.trackmyvehicle.view.customview.LineChartHandler
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.vivekkaushik.datepicker.OnDateSelectedListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_speed_report.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class SpeedReportFragment : Fragment() {

    var lastMovedDate : DateTime? = null
    var lastMovedBstId : String? = null

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    private var mSelectedDateTime: DateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VehicleTrackApplication.appComponent?.inject(this)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_speed_report, container, false)
    }

    private var mLineChart: LineChartHandler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateHeader()
        datePickerTimeline.configureForApp(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                mSelectedDateTime = DateTime.now().withMonthOfYear(month + 1)
                        .withYear(year)
                        .withDayOfMonth(day)
                mSelectedDateTime?.toDate()?.let { onDateSelected(it) }
            }

            override fun onDisabledDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int, isDisabled: Boolean) {

            }
        })

        imgClose.setOnClickListener { this.activity?.finish() }

        imgChangeVehicle.setOnClickListener {
            showVehicleSelectDialog()
        }
        txtCurrentBstId.setOnClickListener {
            showVehicleSelectDialog()
        }

        val floats: MutableList<Float> = ArrayList()
        val dates: MutableList<DateTime> = ArrayList()

        mLineChart = LineChartHandler(
                lineChartSpeedReport,
                "time",
                "speed",
                "pref",
                dates,
                floats
        )

        onDateSelected(Date())

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mLineChart?.setOffset(p1.toFloat() / 100)
                Log.d("TAG", "$p1")
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        txtCurrentBstId.setOnTouchListener(object : OnGestureListener(requireContext()) {
            override fun onSwipeBottom() {
                mVehicleViewModel.selectNextVehicle()

            }

            override fun onSwipeTop() {
                mVehicleViewModel.selectPreviousVehicle()
            }
        })
    }

    private fun showVehicleSelectDialog() {
        val dialog = BottomNavFragment.newInstance()
        dialog.show(childFragmentManager, "BottomNavFragment")
    }

    private fun onDateSelected(date: Date) {
        mSelectedDateTime = DateTime(date)
        txtDate.text = mSelectedDateTime?.toString("d")
        txtMonth.text = mSelectedDateTime?.toString("MMM")?.toUpperCase()
        getCurrentVehicleRoutes(mSelectedDateTime ?: DateTime.now())
    }

    companion object {
        fun newInstance(): SpeedReportFragment {
            val fragment = SpeedReportFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
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

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        updateHeader()
        mSelectedDateTime?.let { getCurrentVehicleRoutes(it) }
    }

    private fun updateHeader() {
        txtCurrentBstId.text = mVehicleViewModel.mPrefRepository.currentVehicle()
    }

    private var mCurrentVehicleRouteLiveData: LiveData<Resource<List<VehicleRoute>>>? = null
    private val mCurrentVehicleRouteObserver = Observer<Resource<List<VehicleRoute>>> { data ->
        if (data == null)
            return@Observer
        val reportData = data.data ?: listOf()

        val speeds = FloatArray(1440)
        val dates = Array<DateTime>(1440) { it ->
            DateTime(mSelectedDateTime)
                    .withMinuteOfHour(it % 60)
                    .withHourOfDay(it / 60)
        }

        reportData.forEach {
            val index = it.updatedAtDate()?.minuteOfDay ?: 0
            speeds[index] = it.speed.toFloatOrNull() ?: 0.toFloat()
        }

        mLineChart?.data = speeds.toMutableList()
        mLineChart?.values = dates.toMutableList()



        if (reportData.isEmpty() && data?.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
            mLineChart?.setUpBarChart()
            moveChartIndexToHeighestSpeed()
        }

        if (reportData.isEmpty() && data?.status == Status.LOADING) {
            Toasty.warning(requireContext(), "Report is loading").show()
        } else if (reportData.isEmpty() && data?.status == Status.ERROR) {
            Toasty.error(requireContext(), data?.message ?: "Could not load distance report").show()
        } else if (reportData.isEmpty() && data?.status == Status.SUCCESS) {
            Toasty.warning(requireContext(), "Report data is not available").show()
        } else if (reportData.isNotEmpty() && data?.status == Status.SUCCESS) {
            Toasty.success(requireContext(), "Report data is loaded").show()
        }


    }

    private fun moveChartIndexToHeighestSpeed() {
        val currentVehicle = mVehicleViewModel.mPrefRepository.currentVehicle()
        if(lastMovedDate?.withTimeAtStartOfDay()?.millis == mSelectedDateTime?.withTimeAtStartOfDay()?.millis
                && lastMovedBstId == currentVehicle) {
            return
        }

        val max = mLineChart?.data?.maxOrNull() ?: 0f
        if(max == 0f) return
        val index = mLineChart?.data?.indexOfFirst { it == max } ?: -1
        if(index >= 0) {
            val x = index.toFloat() / (mLineChart?.data?.size ?: 1)
            seekbar.progress = (x*100).toInt()
            mLineChart?.setOffset(x)
            lastMovedDate = mSelectedDateTime

            lastMovedBstId = currentVehicle
        }
    }

    private fun showChart() {
        try {
            lineChartSpeedReport.visibility = View.VISIBLE
            seekbar.visibility = View.VISIBLE
            helperText.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

        }catch (ex: Exception){
            FirebaseCrashlytics.getInstance().recordException(ex)
        }

    }

    private fun hideChart() {
        lineChartSpeedReport.visibility = View.GONE
        seekbar.visibility = View.GONE
        helperText.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }


    private fun getCurrentVehicleRoutes(date: DateTime) {

        mCurrentVehicleRouteLiveData?.removeObserver(mCurrentVehicleRouteObserver)

        mCurrentVehicleRouteLiveData = mVehicleViewModel
                .getCurrentVehicleRoutes(date.toString("yyyy-MM-dd"))

        mCurrentVehicleRouteLiveData?.observe(this, mCurrentVehicleRouteObserver)
    }
}