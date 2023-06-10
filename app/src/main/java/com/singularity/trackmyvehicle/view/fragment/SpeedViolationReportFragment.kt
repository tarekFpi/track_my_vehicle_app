package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.SpeedViolationAdapter
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.twinkle94.monthyearpicker.picker.YearMonthPickerDialog
import kotlinx.android.synthetic.main.fragment_speed_violation_report.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import javax.inject.Inject

class SpeedViolationReportFragment : Fragment() {

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel

    private var mSelectedDateTime: DateTime? = null
    private var mSpeedViolatointLiveData: MutableLiveData<Resource<List<SpeedAlertReport>>>? = null
    private val mSpeedViolationObserver = Observer<Resource<List<SpeedAlertReport>>> { data ->
        speedList.visibility = View.VISIBLE
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
        swipeRefreshLayout.visibility = View.VISIBLE

        when (data?.status) {
            Status.SUCCESS -> {
                swipeRefreshLayout.isRefreshing = false
            }
            Status.ERROR   -> {
                swipeRefreshLayout.isRefreshing = false
            }
            Status.LOADING -> {
                swipeRefreshLayout.isRefreshing = true
            }
        }


    }

    private val mSpeedViolationAdapter = SpeedViolationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
        VehicleTrackApplication.appComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_speed_violation_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtDate.setOnClickListener {
            val yearMonthPickerDialog = YearMonthPickerDialog(this@SpeedViolationReportFragment.context, YearMonthPickerDialog.OnDateSetListener { year, month ->
                mSelectedDateTime = DateTime.now().withMonthOfYear(month + 1)
                        .withYear(year)
                updateDateLabels()

                updateSpeedViolationReport()
            })
            yearMonthPickerDialog.show();
        }
        containerVehicleInfo.setOnClickListener {
            showVehicleSelectDialog()
        }
        imgVehicle.setOnClickListener {
            showVehicleSelectDialog()
        }
        containerVehicleNames.setOnClickListener {
            showVehicleSelectDialog()
        }
        mSelectedDateTime = DateTime.now()
        updateHeader()
        updateDateLabels()
        updateSpeedViolationReport()
        speedReportList.layoutManager = LinearLayoutManager(requireContext())
        speedReportList.adapter = mSpeedViolationAdapter
        speedReportList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        swipeRefreshLayout.setOnRefreshListener {
            updateSpeedViolationReport()
        }
    }

    private fun updateSpeedViolationReport() {
        mSpeedViolatointLiveData?.removeObserver(mSpeedViolationObserver)
        mSpeedViolatointLiveData = mReportsViewModel.fetchCurrentVehicleSpeedViolatoint(mSelectedDateTime
                ?: DateTime.now())

        mSpeedViolatointLiveData?.observe(this, mSpeedViolationObserver)


    }

    private fun showVehicleSelectDialog() {
        val dialog = BottomNavFragment.newInstance()
        dialog.show(childFragmentManager, "BottomNavFragment")
    }

    private fun updateDateLabels() {
        txtDate.setText(mSelectedDateTime?.toString("MMMM yyyy"))

    }

    companion object {
        fun newInstance(): SpeedViolationReportFragment {
            val fragment = SpeedViolationReportFragment()
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
        updateSpeedViolationReport()

    }

    private fun updateHeader() {
        txtVehicleName.text = mVehicleViewModel.mPrefRepository.currentVehicle()
        txtVehicleTitle.text = mVehicleViewModel.mPrefRepository.currentVehicleVrn()
    }


}