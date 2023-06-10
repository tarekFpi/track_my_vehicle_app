package com.singularity.trackmyvehicle.view.fragment

import android.graphics.Color
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
import kotlinx.android.synthetic.main.item_engine_timeline.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class LocationReportFragment : Fragment() {

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

    private val mAdapter = LocationTimeLineAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtBanner.text = "LOCATION REPORT"
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
        fun newInstance(): LocationReportFragment {
            val fragment = LocationReportFragment()
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
        val data = it.data ?: listOf()

        val result = mutableListOf<LocationTimeLineModel>()
        val length = it?.data?.size ?: 0
        for (i in 0 until length) {
            val currentIter = data.get(i)
            if ((currentIter.location?.place ?: "") == "") {
                continue
            }
            val previousIter: VehicleRoute? = if (i == 0) null else data.get(i - 1)
            if (currentIter?.location?.place != previousIter?.location?.place) {
                result.add(
                        LocationTimeLineModel(
                                currentIter.updatedAtDate(),
                                currentIter.location?.place ?: ""
                        )
                )
            }
        }

        mAdapter.setItems(result)
        if (data.isEmpty() && it.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
        }

        if (it.status == Status.LOADING) {
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

data class LocationTimeLineModel(
        var time: DateTime?,
        var location: String
)

class LocationTimeLineAdapter : RecyclerView.Adapter<LocationTimeLineAdapter.TimeLineViewHolder>() {

    private var mData = mutableListOf<LocationTimeLineModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_engine_timeline, parent, false)
        return TimeLineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    fun setItems(data: MutableList<LocationTimeLineModel>) {
        mData = data
        notifyDataSetChanged()
    }

    override fun getItemCount() = mData.size

    inner class TimeLineViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        val status: List<Boolean> = (0..9).map { Math.random() < 0.5 }

        val date = containerView.txtTimeStart
        val message = containerView.txtEngineStatus
        val timeline = containerView.timeline

        fun bind(model: LocationTimeLineModel) {
            val viewType = when (adapterPosition) {
                0 -> 1 // Start
                mData.size - 1 -> 2 // END
                else -> 0
            }
            timeline.initLine(viewType)
            timeline.setEndLineColor(Color.parseColor("#4CAF50"), viewType)
            timeline.setMarkerColor(Color.parseColor("#4CAF50"))
            timeline.setStartLineColor(Color.parseColor("#4CAF50"), viewType)
            date.text = model.time?.toString(DateTimeFormat.forPattern("hh:mm a"))
            message.text = model.location
        }
    }

}