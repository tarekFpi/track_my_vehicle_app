package com.singularity.trackmyvehicle.view.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.TripReportResponse
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.view.adapter.TripReportAdapter
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import kotlinx.android.synthetic.main.activity_trip_report.*
import kotlinx.android.synthetic.main.activity_trip_report.layout_date
import kotlinx.android.synthetic.main.activity_trip_report.textView_bstId
import kotlinx.android.synthetic.main.activity_trip_report.textView_startDate
import kotlinx.android.synthetic.main.item_trip_report.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class TripReportActivity : AppCompatActivity(){

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    private lateinit var adapter : TripReportAdapter

    var cookie : String = ""

    var currentVehicle = ""

    var terminalId : String = ""

    var selectedStartDate : String = ""

    var selectedEndDate : String = ""

    var tripReportDataList : ArrayList<TripReportResponse.ReportSuccessResponse.ReportDataList> = ArrayList()

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_report)

        cookie = intent.getStringExtra("cookie").toString()

        adapter = TripReportAdapter(this@TripReportActivity, tripReportDataList, textView_bstId.text.toString())
        recyclerView_tripReport.setHasFixedSize(true)
        recyclerView_tripReport.adapter = adapter

        bottomNavShow()

        changeCurrentVehicle()

        selectDateRange()

        setDefaultDateRange()

        backPressed()

        searchTripReport()
    }

    private fun searchTripReport() {
        textView_search_trip_report.setOnClickListener {
            getTripReportResponse()
        }
    }

    private fun getTripReportResponse() {
        showLoading()

        val call = RetrofitClient.getInstance(cookie)
                ?.create(ApiHandler::class.java)?.getTripReportList(
                        ENDPOINTS.TRIP_REPORT,
                        terminalId,
                        selectedStartDate,
                        selectedEndDate
                )

        call?.enqueue(object : Callback<TripReportResponse> {
            override fun onResponse(call: Call<TripReportResponse>, response: Response<TripReportResponse>) {
                if (!this@TripReportActivity.isDestroyed) dismissDialog()

                loadTripReportList(response.body()?.reportSuccessResponse?.reportDataList ?: ArrayList())

                if(response.body()?.reportErrorResponse?.code != 0) {
                    showDialogError(response.body()?.reportErrorResponse?.description ?: "Something went wrong, Please try again")
                }
            }

            override fun onFailure(call: Call<TripReportResponse>, t: Throwable) {
                if (!this@TripReportActivity.isDestroyed) dismissDialog()
                loadTripReportList(ArrayList())
                showDialogError("Something went wrong, Please try again")
            }
        })
    }

    private fun loadTripReportList(reportDataList: List<TripReportResponse.ReportSuccessResponse.ReportDataList>) {
        imageView_empty_report.visibility = if (reportDataList.isEmpty()) View.VISIBLE else View.GONE

        tripReportDataList.clear()
        tripReportDataList.addAll(reportDataList)

        adapter.notifyDataSetChanged()
    }

    private fun setDefaultDateRange() {
        var simpleFormat = SimpleDateFormat("E, MMM dd, yyyy")

        val currentDate = simpleFormat.format(Date())

        var customizeFormat = SimpleDateFormat("yyyy-MM-dd")

        selectedStartDate = customizeFormat.format(Date()).toString()+" "+"00:00:00"
        selectedEndDate = customizeFormat.format(Date()).toString()+" "+"23:59:59"

        textView_startDate.text = currentDate.toString()
        textView_endDate.text = currentDate.toString()
    }

    private fun bottomNavShow() {

        layout_bottomNav.setOnClickListener {

            val dialogFrag = BottomNavFragment()
           dialogFrag.show(supportFragmentManager, dialogFrag.tag)


        }

    }

    private fun backPressed() {
        imageView_cancelIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun selectDateRange() {
        val materialDateBuilder = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("SELECTED RANGE")
                .build()

        layout_date.setOnClickListener {
            materialDateBuilder.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDateBuilder.addOnPositiveButtonClickListener {

            var startDate = it.first
            var endDate = it.second

            var simpleFormat = SimpleDateFormat("E, MMM dd, yyyy")

            var customizeFormat = SimpleDateFormat("yyyy-MM-dd")

            selectedStartDate = customizeFormat.format(startDate).toString()+" "+"00:00:00"
            selectedEndDate = customizeFormat.format(endDate).toString()+" "+"23:59:59"

            textView_startDate.text = simpleFormat.format(startDate).toString()
            textView_endDate.text = simpleFormat.format(endDate).toString()

        }
    }

    override fun onResume() {
        super.onResume()
        if (! EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        runOnUiThread {
            Log.d("ujyg", "onvehiclechange 1")
            changeCurrentVehicle()
        }
    }

    private fun changeCurrentVehicle() {

        currentVehicle = mVehicleViewModel.mPrefRepository.currentVehicle()
        terminalId = mVehicleViewModel.mPrefRepository.currentVehicleTerminalId
        textView_bstId.text = if (currentVehicle.isEmpty()) "Loading..." else currentVehicle.trim()

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
