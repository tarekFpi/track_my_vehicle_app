package com.singularity.trackmyvehicle.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.utils.Log
import com.singularity.trackmyvehicle.view.adapter.AnalyticsActiveVehiclesAdapter
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_active_vehicle_list.*
import kotlinx.android.synthetic.main.fragment_analytics.*
import kotlinx.android.synthetic.main.mergedappbarlayout.*

class ActiveVehicleListActivity : AppCompatActivity() {

    private var mDialog: MaterialDialog? = null

    val script : String = "API/V1/Analytics"

    var cookie : String = ""

    var analyticsActiveVehicleDataList : ArrayList<AnalyticsResponse.AnalyticsTerminal> = ArrayList() //off

    var terminalDataList : ArrayList<AnalyticsResponse.AnalyticsTerminal> = ArrayList()

    private lateinit var appPreference : AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_vehicle_list)

        appPreference = AppPreferenceImpl(this)

        setBackButton()

        getActiveVehicleListResponse()
    }

    private fun setBackButton() {
        imageView_backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getActiveVehicleListResponse() {

        terminalDataList = appPreference.getAnalyticsTerminalList(AppPreference.AnalyticsTerminalList) as ArrayList<AnalyticsResponse.AnalyticsTerminal>

        getAllVehiclesData(terminalDataList)

        getLiveVehiclesData(terminalDataList)

        getSuspendedVehiclesData(terminalDataList)

        getOfflineVehiclesData(terminalDataList)

        analyticsActiveVehicleDataList.clear()
        analyticsActiveVehicleDataList.addAll(terminalDataList)
        var adapter = this@ActiveVehicleListActivity?.let { AnalyticsActiveVehiclesAdapter(it, analyticsActiveVehicleDataList) }
        recyclerView_activeVehicles.isNestedScrollingEnabled = false
        recyclerView_activeVehicles.setHasFixedSize(true)
        recyclerView_activeVehicles.adapter = adapter
        adapter?.notifyDataSetChanged()

    }

    private fun getOfflineVehiclesData(analyticsOfflineVehicleDataList: List<AnalyticsResponse.AnalyticsTerminal>) {
        textView_Offline.setOnClickListener {
            analyticsActiveVehicleDataList.clear()
            for(item in analyticsOfflineVehicleDataList){
                if(item.terminalState == "Offline" && item.terminalAssignmentIsSuspended == "0"){
                    analyticsActiveVehicleDataList.add(item)
                }
            }
            var adapter = this@ActiveVehicleListActivity?.let { AnalyticsActiveVehiclesAdapter(it, analyticsActiveVehicleDataList) }
            recyclerView_activeVehicles.isNestedScrollingEnabled = false
            recyclerView_activeVehicles.setHasFixedSize(true)
            recyclerView_activeVehicles.adapter = adapter
            adapter?.notifyDataSetChanged()

            cardView_offline.visibility = View.VISIBLE
            cardView_suspended.visibility = View.GONE
            cardView_Live.visibility = View.GONE
            cardView_allVehicles.visibility = View.GONE

            textView_Offline.visibility = View.GONE
            textView_suspended.visibility = View.VISIBLE
            textView_Live.visibility = View.VISIBLE
            textView_allVehicles.visibility = View.VISIBLE
        }
    }

    private fun getSuspendedVehiclesData(analyticsSuspendedVehicleDataList: List<AnalyticsResponse.AnalyticsTerminal>) {
        textView_suspended.setOnClickListener {
            analyticsActiveVehicleDataList.clear()
            for(item in analyticsSuspendedVehicleDataList){
                if(item.terminalState =="Suspended" || item.terminalAssignmentIsSuspended =="1"){

                     analyticsActiveVehicleDataList.add(item)

                }
            }
            var adapter = this@ActiveVehicleListActivity?.let { AnalyticsActiveVehiclesAdapter(it, analyticsActiveVehicleDataList) }
            recyclerView_activeVehicles.isNestedScrollingEnabled = false
            recyclerView_activeVehicles.setHasFixedSize(true)
            recyclerView_activeVehicles.adapter = adapter
            adapter?.notifyDataSetChanged()

            cardView_suspended.visibility = View.VISIBLE
            cardView_Live.visibility = View.GONE
            cardView_allVehicles.visibility = View.GONE
            cardView_offline.visibility = View.GONE

            textView_suspended.visibility = View.GONE
            textView_Live.visibility = View.VISIBLE
            textView_allVehicles.visibility = View.VISIBLE
            textView_Offline.visibility = View.VISIBLE
        }
    }

    private fun getLiveVehiclesData(analyticsLiveVehicleDataList: List<AnalyticsResponse.AnalyticsTerminal>) {
        textView_Live.setOnClickListener {
            analyticsActiveVehicleDataList.clear()
            for(item in analyticsLiveVehicleDataList){
                if(item.terminalState == "Live" && item.terminalAssignmentIsSuspended == "0"){
                    analyticsActiveVehicleDataList.add(item)
                }
            }
            var adapter = this@ActiveVehicleListActivity?.let { AnalyticsActiveVehiclesAdapter(it, analyticsActiveVehicleDataList) }
            recyclerView_activeVehicles.isNestedScrollingEnabled = false
            recyclerView_activeVehicles.setHasFixedSize(true)
            recyclerView_activeVehicles.adapter = adapter
            adapter?.notifyDataSetChanged()

            cardView_Live.visibility = View.VISIBLE
            cardView_allVehicles.visibility = View.GONE
            cardView_suspended.visibility = View.GONE
            cardView_offline.visibility = View.GONE

            textView_Live.visibility = View.GONE
            textView_allVehicles.visibility = View.VISIBLE
            textView_suspended.visibility = View.VISIBLE
            textView_Offline.visibility = View.VISIBLE

        }
    }

    private fun getAllVehiclesData(analyticsAllVehicleDataList: List<AnalyticsResponse.AnalyticsTerminal>) {
        textView_allVehicles.setOnClickListener {
            analyticsActiveVehicleDataList.clear()
            analyticsActiveVehicleDataList.addAll(analyticsAllVehicleDataList)
            var adapter = this@ActiveVehicleListActivity?.let { AnalyticsActiveVehiclesAdapter(it, analyticsActiveVehicleDataList) }
            recyclerView_activeVehicles.isNestedScrollingEnabled = false
            recyclerView_activeVehicles.setHasFixedSize(true)
            recyclerView_activeVehicles.adapter = adapter
            adapter?.notifyDataSetChanged()

            cardView_allVehicles.visibility = View.VISIBLE
            cardView_Live.visibility = View.GONE
            cardView_suspended.visibility = View.GONE
            cardView_offline.visibility = View.GONE

            textView_allVehicles.visibility = View.GONE
            textView_Live.visibility = View.VISIBLE
            textView_suspended.visibility = View.VISIBLE
            textView_Offline.visibility = View.VISIBLE
        }
    }

    private fun showLoading() {
        dismissDialog()
        mDialog = this@ActiveVehicleListActivity?.let {
            DialogHelper.getLoadingDailog(it, getString(R.string.msg_hold_on),
                    getString(R.string.msg_loading))
                    ?.show()
        }
    }

    private fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }

    private fun showDialogError(msg: String) {
        dismissDialog()
        mDialog = this@ActiveVehicleListActivity?.let {
            DialogHelper.getMessageDialog(it, getString(R.string.title_error), msg)
                    ?.positiveText(getString(R.string.action_ok))
                    ?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        appPreference.setAnalyticsTerminalList(AppPreference.AnalyticsTerminalList, ArrayList())
    }
}