package com.singularity.trackmyvehicle.view.fragment

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaurav.gesto.OnGestureListener
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.activity.configureForApp
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.vivekkaushik.datepicker.OnDateSelectedListener
import es.dmoral.toasty.Toasty
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_daily_engine_report.*
import kotlinx.android.synthetic.main.fragment_feedback_list.*
import kotlinx.android.synthetic.main.item_engine_pie_chart.view.*
import kotlinx.android.synthetic.main.item_engine_timeline.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.lang.Float.NaN


class DailyEngineReportFragment : Fragment() {

    private var mEngineReportLiveData: MutableLiveData<Resource<List<VehicleRoute>>>? = null

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel

    private var mSelectedDateTime: DateTime? = null


    private fun showChart() {
        rvEngineOfTimeLine.visibility = if (mAdapter.itemCount == 0) View.GONE else View.VISIBLE
        txtEmptyText.visibility = if (mAdapter.itemCount != 0) View.GONE else View.VISIBLE
        progressBar.visibility = View.GONE

    }

    private fun hideChart() {
        rvEngineOfTimeLine.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        txtEmptyText.visibility = View.GONE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_daily_engine_report, container, false)
    }

    private val mAdapter = TimeLineAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtBanner.text = "ENGINE REPORT"
        datePickerTimeline.configureForApp(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                mSelectedDateTime = DateTime.now().withMonthOfYear(month + 1)
                        .withYear(year)
                        .withDayOfMonth(day)
                updateDateLabels()
                updateEngineReport()
            }

            override fun onDisabledDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int,
                                                isDisabled: Boolean) {

            }
        })

        imgClose.setOnClickListener { this.activity?.finish() }

        imgChangeVehicle.setOnClickListener {
            showVehicleSelectDialog()
        }
        txtCurrentBstId.setOnClickListener {
            showVehicleSelectDialog()
        }
        hideChart()
        mSelectedDateTime = DateTime.now()
        updateHeader()
        updateDateLabels()
        updateEngineReport()

        showChart()

        rvEngineOfTimeLine.layoutManager = LinearLayoutManager(requireContext())

        rvEngineOfTimeLine.adapter = mAdapter

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

    private fun updateDateLabels() {
        txtMonth.text = mSelectedDateTime?.toString("MMM")
        txtDate.text = mSelectedDateTime?.toString("dd")

    }

    companion object {
        fun newInstance(): DailyEngineReportFragment {
            val fragment = DailyEngineReportFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
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
        updateEngineReport()

    }

    private fun updateHeader() {
        txtCurrentBstId.text = mVehicleViewModel.mPrefRepository.currentVehicle()
    }

    private val mObserver = Observer<Resource<List<VehicleRoute>>> {
        val data = it.data?.sortedByDescending { d -> d.updatedAtDate()?.millis } ?: listOf()

        val result = mutableListOf<TimeLineModel>()
        val length = it?.data?.size ?: 0
        var onMinute = 0
        var offMinute = 0
        var idleMinute = 0
        for (i in 0 until length) {
            val currentIter = data.get(i)
            if ((currentIter.engineStatus ?: "") == "") {
                idleMinute++
                continue
            } else {
                if (currentIter.engineStatus == "ON") {
                    onMinute++
                } else {
                    offMinute++
                }
            }
            val previousIter: VehicleRoute? = if (i == length - 1) null else data.get(i + 1)
            if (currentIter.engineStatus != previousIter?.engineStatus) {
                result.add(
                        TimeLineModel(currentIter.engineStatus, currentIter.updatedAtDate(),
                                currentIter.location?.place ?: "",
                                previousIter?.engineStatus
                                        ?: "", previousIter?.updatedAtDate())
                )
            }
        }
        val summary = EngineOnSummaryModel(onMinute, offMinute, idleMinute)
        mAdapter.setItems(result, summary)
        if (data.isEmpty() && it.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
        }

        if (it.status == Status.LOADING && data.isEmpty()) {
            Toasty.warning(requireContext(), "Loading information").show()
            return@Observer
        }
        if (it.status == Status.SUCCESS && data.isNullOrEmpty()) {
            Toasty.warning(requireContext(), "Vehicle data not available").show()
        } else if (it.status == Status.SUCCESS) {
            Toasty.success(requireContext(), "Information loaded").show()
            return@Observer
        }
        if (it.status == Status.ERROR) {
            Toasty.error(requireContext(), it.message ?: "Engine report loading failed").show()
        }

    }

    private fun updateEngineReport() {

        mEngineReportLiveData?.removeObserver(mObserver)
        mEngineReportLiveData = mVehicleViewModel.getCurrentVehicleRoutes((mSelectedDateTime
                ?: DateTime.now()).toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

        mEngineReportLiveData?.observe(this, mObserver)

    }

}

data class TimeLineModel(
        var status: String,
        var time: DateTime?,
        var location: String,
        var previousStatus: String,
        var previousTime: DateTime?
)

data class EngineOnSummaryModel(
        val onMinute: Int,
        val offMinute: Int,
        val idleMinute: Int
)

class TimeLineAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TIMELINE = 123
        const val TYPE_PIE_CHART = 122
    }

    private var mData = mutableListOf<TimeLineModel>()
    private var mSummary: EngineOnSummaryModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TIMELINE -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_engine_timeline, parent, false)
                TimeLineViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_engine_pie_chart, parent, false)
                PieChartViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TimeLineViewHolder)
            holder.bind(mData[position])

        if (holder is PieChartViewHolder)
            mSummary?.let { holder.bind(it) }

    }

    fun setItems(data: MutableList<TimeLineModel>, summary: EngineOnSummaryModel) {
        mData = data
        mSummary = summary
        notifyDataSetChanged()
    }

    override fun getItemCount() = if (mData.size == 0) 0 else (mData.size + 1)

    override fun getItemViewType(position: Int): Int {
        return if (mData.size == position) {
            TYPE_PIE_CHART
        } else {
            TYPE_TIMELINE
        }
    }

    inner class TimeLineViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        val status: List<Boolean> = (0..9).map { Math.random() < 0.5 }

        val date = containerView.txtTimeStart
        val message = containerView.txtEngineStatus
        val timeline = containerView.timeline

        fun bind(model: TimeLineModel) {
            val viewType = when (adapterPosition) {
                0 -> 1 // Start
                mData.size - 1 -> 2 // END
                else -> 0
            }
            timeline.initLine(viewType)
            if (model.status == "ON") {
                timeline.setStartLineColor(Color.parseColor("#4CAF50"), viewType)
                timeline.setMarkerColor(Color.parseColor("#4CAF50"))
            } else {
                timeline.setStartLineColor(Color.parseColor("#FF0000"), viewType)
                timeline.setMarkerColor(Color.parseColor("#FF0000"))
            }
            if (model.previousStatus == "ON") {
                timeline.setEndLineColor(Color.parseColor("#4CAF50"), viewType)
            } else {
                timeline.setEndLineColor(Color.parseColor("#FF0000"), viewType)
            }
            date.text = model.time?.toString(DateTimeFormat.forPattern("hh:mm a"))
            if (model.location.isEmpty()) {
                message.text = if (model.status == "ON") "Engine was turned ON" else "Engine was turned OFF"
            } else {
                message.text = if (model.status == "ON") "Engine was turned ON near ${model.location}" else "Engine was turned OFF near ${model.location}"
            }
        }
    }

    inner class PieChartViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {
        val chart: PieChart = containerView.pieChart

        init {
            chart.setBackgroundColor(Color.WHITE)
            chart.setUsePercentValues(true)
            chart.description.isEnabled = false

            chart.isDrawHoleEnabled = true
            chart.setHoleColor(Color.WHITE)

            chart.setTransparentCircleColor(Color.WHITE)
            chart.setTransparentCircleAlpha(110)

            chart.holeRadius = 0f//58f;
            chart.transparentCircleRadius = 0f//61f;

            chart.setDrawCenterText(false)

            chart.isRotationEnabled = false
            chart.isHighlightPerTapEnabled = true

            chart.maxAngle = 360f // HALF CHART
            chart.rotationAngle = 360f
            chart.setCenterTextOffset(0F, -20F)

        }

        fun bind(summary: EngineOnSummaryModel) {
            val values = mutableListOf<PieEntry>()

            values.add(PieEntry(summary.onMinute.toFloat(), "ON"))
            values.add(PieEntry(summary.offMinute.toFloat(), "OFF"))
            values.add(PieEntry(summary.idleMinute.toFloat(), "IDLE"))

            val dataSet = PieDataSet(values, "Engine Report Summary")
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 5f
            dataSet.setColors(Color.parseColor("#4CAF50"), Color.RED, Color.YELLOW)
            dataSet.setAutomaticallyDisableSliceSpacing(true)

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter())
            data.setValueTextSize(11f)
            data.setDrawValues(false)
            data.setValueTextColor(Color.WHITE)

            val legends = chart.legend
            chart.setDrawEntryLabels(false)
            legends.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legends.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legends.orientation = Legend.LegendOrientation.HORIZONTAL
            legends.setDrawInside(false)
            legends.xEntrySpace = 7f
            legends.yEntrySpace = 0f
            legends.yOffset = 0f

            chart.data = data
            chart.legend.isWordWrapEnabled = true
            chart.invalidate()
        }
    }

}