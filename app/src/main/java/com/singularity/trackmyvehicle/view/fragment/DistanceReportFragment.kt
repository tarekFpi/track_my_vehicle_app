package com.singularity.trackmyvehicle.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gaurav.gesto.OnGestureListener
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.DistanceAdapter
import com.singularity.trackmyvehicle.view.customview.BarChartHandler
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.whiteelephant.monthpicker.MonthPickerDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_distance_report.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class DistanceReportFragment : Fragment() {

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel

    private var mSelectedDateTime: DateTime? = null

    private var mAdapter: DistanceAdapter = DistanceAdapter()

    private var mDistanceReportLiveData: MutableLiveData<Resource<List<DistanceReport>>>? = null

    var isTableVisible = false

    private val mDistanceReportObserver = Observer<Resource<List<DistanceReport>>> { data ->
        val reportData = data?.data ?: listOf()
        // update chart
        val dates: ArrayList<DateTime> = ArrayList()
        val floats: ArrayList<Float> = ArrayList()

        val tableReport: MutableList<DistanceReport>? = mutableListOf()

        mSelectedDateTime?.let { selectedDate ->
            if (reportData.isNotEmpty()) {
                for (i in selectedDate.dayOfMonth().minimumValue..selectedDate.dayOfMonth().maximumValue) {
                    dates.add(selectedDate.withDate(selectedDate.year, selectedDate.monthOfYear, i))
                    floats.add(reportData.firstOrNull {
                        DateTime.parse(it.date,
                                DateTimeFormat.forPattern("yyyy-MM-dd")).dayOfMonth == i
                    }?.km?.toFloatOrNull() ?: 0F)
                }
            }
        }

        for (i in 0 until floats.size) {
            val model = DistanceReport()
            model.date = dates[i].toString("yyyy-MM-dd HH:mm:ss")
            model.km = floats[i].toString()
            tableReport?.add(model)
        }

        mAdapter.setData(tableReport)

        BarChartHandler(barChartDistance,
                "Date: ",
                "Distance: ",
                "",
                dates,
                floats,
                unitInMeter = false
        )

        if (reportData.isEmpty() && data?.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
        }

        if (reportData.isEmpty() && data?.status == Status.LOADING) {
            Toasty.warning(requireContext(), "Report is loading").show()
        } else if (reportData.isEmpty() && data?.status == Status.ERROR) {
            Toasty.error(requireContext(), data?.message ?:"Could not load distance report").show()
        } else if (reportData.isEmpty() && data?.status == Status.SUCCESS) {
            Toasty.warning(requireContext(), "Report data is not available").show()
        } else if (reportData.isNotEmpty() && data?.status == Status.SUCCESS) {
            Toasty.success(requireContext(), "Report data is loaded").show()
        }


    }


    private fun showChart() {
        progressBar.visibility = View.GONE
        barChartDistance.visibility = if (isTableVisible) View.GONE else View.VISIBLE
        tableCharDistance.visibility = if (isTableVisible) View.VISIBLE else View.GONE

    }

    private fun hideChart() {
        barChartDistance.visibility = View.GONE
        tableCharDistance.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_distance_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerMonthAndYear.setOnClickListener {

            val builder = MonthPickerDialog.Builder(requireContext(),
                    MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                        mSelectedDateTime = DateTime.now().withMonthOfYear(selectedMonth + 1)
                                .withYear(selectedYear)
                        updateDateLabels()

                        updateDistanceReport()
                    },
                    mSelectedDateTime?.year ?: DateTime.now().year,
                    (mSelectedDateTime?.monthOfYear ?: DateTime.now().monthOfYear) - 1)
            builder.setTitle("Select Month")
            builder.setMinYear(DateTime.now().minusMonths(3).year)
            builder.setMaxYear(DateTime.now().year)
            builder.build().show()

        }

        imgClose.setOnClickListener { this.activity?.finish() }

        imgChangeVehicle.setOnClickListener {
            showVehicleSelectDialog()
        }
        txtCurrentBstId.setOnClickListener {
            showVehicleSelectDialog()
        }
        setRecyclerView()
        barChartDistance.setPinchZoom(false)
        barChartDistance.setScaleEnabled(true)
        hideChart()
        mSelectedDateTime = DateTime.now()
        updateHeader()
        updateDateLabels()
        updateDistanceReport()
        hideTable()
        btnViewChange.setOnClickListener {
            changeView()
        }
        txtCurrentBstId.setOnTouchListener(object : OnGestureListener(requireContext()) {
            override fun onSwipeBottom() {
                mVehicleViewModel.selectNextVehicle()

            }

            override fun onSwipeTop() {
                mVehicleViewModel.selectPreviousVehicle()
            }
        })
    }

    private fun setRecyclerView() {
        rvTableDistance.adapter = mAdapter
        rvTableDistance.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun changeView() {
        if (!isTableVisible) {
            showTable()
        } else {
            hideTable()
        }

    }

    private fun hideTable() {
        tableCharDistance.visibility = View.GONE
        barChartDistance.visibility = View.VISIBLE
        btnViewChange.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_black_24dp))
        isTableVisible = false
    }

    private fun showTable() {
        barChartDistance.visibility = View.GONE
        tableCharDistance.visibility = View.VISIBLE
        btnViewChange.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_show_chart_black_24dp))
        isTableVisible = true
    }


    private fun showVehicleSelectDialog() {
        val dialog = BottomNavFragment.newInstance()
        dialog.show(childFragmentManager, "BottomNavFragment")
    }

    private fun updateDateLabels() {
        txtMonth.text = mSelectedDateTime?.toString("MMM")
        txtYear.text = mSelectedDateTime?.toString("yyyy")

    }

    companion object {
        fun newInstance(): DistanceReportFragment {
            val fragment = DistanceReportFragment()
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
        updateDistanceReport()

    }

    private fun updateHeader() {
        txtCurrentBstId.text = mVehicleViewModel.mPrefRepository.currentVehicle()
    }

    private fun updateDistanceReport() {
        mDistanceReportLiveData?.removeObserver(mDistanceReportObserver)
        mDistanceReportLiveData = mReportsViewModel.fetchCurrentVehicleDistanceReport(
                mSelectedDateTime
                        ?: DateTime.now())
        mDistanceReportLiveData?.observe(this, mDistanceReportObserver)

    }


}