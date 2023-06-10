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
import com.singularity.trackmyvehicle.view.adapter.AnalyticsSafeDrivingAdapter
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import kotlinx.android.synthetic.main.activity_active_vehicle_list.*
import kotlinx.android.synthetic.main.activity_active_vehicle_list.imageView_backButton
import kotlinx.android.synthetic.main.activity_safe_driving_list.*
import java.util.*
import kotlin.collections.ArrayList


class SafeDrivingListActivity : AppCompatActivity() {

    private var mDialog: MaterialDialog? = null

    val script : String = "API/V1/Analytics"

    var cookie : String = ""

    var analyticsSafeDrivingBstidVrnDataList : ArrayList<AnalyticsResponse.AnalyticsTerminal> = ArrayList()

    var analyticsSafeDrivingDataList : ArrayList<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank> = ArrayList()

    var safeDrivingDataList : ArrayList<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank> = ArrayList()

    var terminalDataList : ArrayList<AnalyticsResponse.AnalyticsTerminal> = ArrayList()

    private lateinit var appPreference : AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_driving_list)

        appPreference = AppPreferenceImpl(this)

        setBackButton()

        getSafeDrivingListResponse()
    }

    private fun setBackButton() {
        imageView_backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getSafeDrivingListResponse() {

        safeDrivingDataList = appPreference.getAnalyticsSafeDrivingList(AppPreference.AnalyticsSafeDrivingList) as ArrayList<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>
        terminalDataList = appPreference.getAnalyticsTerminalList(AppPreference.AnalyticsTerminalList) as ArrayList<AnalyticsResponse.AnalyticsTerminal>

        safeDrivingDataList.sortBy { it.terminalID }
        terminalDataList.sortBy { it.terminalID }

        getAllVehiclesData(safeDrivingDataList, terminalDataList)

        getSafeVehiclesData(safeDrivingDataList, terminalDataList)

        getMarginalVehiclesData(safeDrivingDataList, terminalDataList)

        getUnsafeVehiclesData(safeDrivingDataList, terminalDataList)

        analyticsSafeDrivingDataList.clear()
        analyticsSafeDrivingBstidVrnDataList.clear()

        analyticsSafeDrivingDataList.addAll(safeDrivingDataList)
        analyticsSafeDrivingBstidVrnDataList.addAll(terminalDataList)

        var adapter = this@SafeDrivingListActivity?.let { AnalyticsSafeDrivingAdapter(it, analyticsSafeDrivingDataList, analyticsSafeDrivingBstidVrnDataList) }
        recyclerView_safeDriving.isNestedScrollingEnabled = false
        recyclerView_safeDriving.setHasFixedSize(true)
        recyclerView_safeDriving.adapter = adapter
        adapter?.notifyDataSetChanged()

    }

    private fun getUnsafeVehiclesData(unsafeVehiclesData: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>, unsafeVehiclesForBstidVrn: List<AnalyticsResponse.AnalyticsTerminal>) {
        analyticsSafeDrivingDataList.clear()
        analyticsSafeDrivingBstidVrnDataList.clear()

        textView_unsafe.setOnClickListener {
            analyticsSafeDrivingDataList.clear()
            analyticsSafeDrivingBstidVrnDataList.clear()

            for (itemIndex in unsafeVehiclesData.indices) {
                if (unsafeVehiclesData[itemIndex].driveSafetyOrder == "3") {
                    analyticsSafeDrivingDataList.add(unsafeVehiclesData[itemIndex])
                    analyticsSafeDrivingBstidVrnDataList.add(unsafeVehiclesForBstidVrn[itemIndex])
                }
            }

            var adapter = this@SafeDrivingListActivity?.let { AnalyticsSafeDrivingAdapter(it, analyticsSafeDrivingDataList, analyticsSafeDrivingBstidVrnDataList) }
            recyclerView_safeDriving.isNestedScrollingEnabled = false
            recyclerView_safeDriving.setHasFixedSize(true)
            recyclerView_safeDriving.adapter = adapter
            adapter?.notifyDataSetChanged()

            textView_safe.visibility = View.VISIBLE
            textView_unsafe.visibility = View.GONE
            textView_marginal.visibility = View.VISIBLE
            textView_allVehicles_safeDriving.visibility = View.VISIBLE

            cardView_safe.visibility = View.GONE
            cardView_unsafe.visibility = View.VISIBLE
            cardView_marginal.visibility = View.GONE
            cardView_allVehicles_safeDriving.visibility = View.GONE

        }
    }

    private fun getMarginalVehiclesData(marginalVehiclesData: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>, marginalVehiclesForBstidVrn: List<AnalyticsResponse.AnalyticsTerminal>) {
        analyticsSafeDrivingDataList.clear()
        analyticsSafeDrivingBstidVrnDataList.clear()

        textView_marginal.setOnClickListener {
            analyticsSafeDrivingDataList.clear()
            analyticsSafeDrivingBstidVrnDataList.clear()

            for (itemIndex in marginalVehiclesData.indices) {
                if (marginalVehiclesData[itemIndex].driveSafetyOrder == "2") {
                    analyticsSafeDrivingDataList.add(marginalVehiclesData[itemIndex])

                    analyticsSafeDrivingBstidVrnDataList.add(marginalVehiclesForBstidVrn[itemIndex])
                }
            }

            var adapter = this@SafeDrivingListActivity?.let { AnalyticsSafeDrivingAdapter(it, analyticsSafeDrivingDataList, analyticsSafeDrivingBstidVrnDataList) }
            recyclerView_safeDriving.isNestedScrollingEnabled = false
            recyclerView_safeDriving.setHasFixedSize(true)
            recyclerView_safeDriving.adapter = adapter
            adapter?.notifyDataSetChanged()

            textView_safe.visibility = View.VISIBLE
            textView_unsafe.visibility = View.VISIBLE
            textView_marginal.visibility = View.GONE
            textView_allVehicles_safeDriving.visibility = View.VISIBLE

            cardView_safe.visibility = View.GONE
            cardView_unsafe.visibility = View.GONE
            cardView_marginal.visibility = View.VISIBLE
            cardView_allVehicles_safeDriving.visibility = View.GONE

        }
    }

    private fun getSafeVehiclesData(safeVehiclesdata: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>, safeVehiclesForBstidVrn: List<AnalyticsResponse.AnalyticsTerminal>) {
        analyticsSafeDrivingDataList.clear()
        analyticsSafeDrivingBstidVrnDataList.clear()
        textView_safe.setOnClickListener {

            analyticsSafeDrivingDataList.clear()
            analyticsSafeDrivingBstidVrnDataList.clear()

            for (itemIndex in safeVehiclesdata.indices) {
                if (safeVehiclesdata[itemIndex].driveSafetyOrder == "1") {
                    analyticsSafeDrivingDataList.add(safeVehiclesdata[itemIndex])
                    analyticsSafeDrivingBstidVrnDataList.add(safeVehiclesForBstidVrn[itemIndex])
                }
            }

            var adapter = this@SafeDrivingListActivity?.let { AnalyticsSafeDrivingAdapter(it, analyticsSafeDrivingDataList, analyticsSafeDrivingBstidVrnDataList) }
            recyclerView_safeDriving.isNestedScrollingEnabled = false
            recyclerView_safeDriving.setHasFixedSize(true)
            recyclerView_safeDriving.adapter = adapter
            adapter?.notifyDataSetChanged()

            textView_safe.visibility = View.GONE
            textView_unsafe.visibility = View.VISIBLE
            textView_marginal.visibility = View.VISIBLE
            textView_allVehicles_safeDriving.visibility = View.VISIBLE

            cardView_safe.visibility = View.VISIBLE
            cardView_unsafe.visibility = View.GONE
            cardView_marginal.visibility = View.GONE
            cardView_allVehicles_safeDriving.visibility = View.GONE
        }

    }

    private fun getAllVehiclesData(allVehicleSafeDrivingDataList: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>, allVehicleListForBstidVrn: List<AnalyticsResponse.AnalyticsTerminal>) {
        textView_allVehicles_safeDriving.setOnClickListener {
            analyticsSafeDrivingDataList.clear()
            analyticsSafeDrivingBstidVrnDataList.clear()

            analyticsSafeDrivingDataList.addAll(allVehicleSafeDrivingDataList)
            analyticsSafeDrivingBstidVrnDataList.addAll(allVehicleListForBstidVrn)

            var adapter = this@SafeDrivingListActivity?.let { AnalyticsSafeDrivingAdapter(it, analyticsSafeDrivingDataList, analyticsSafeDrivingBstidVrnDataList) }
            recyclerView_safeDriving.isNestedScrollingEnabled = false
            recyclerView_safeDriving.setHasFixedSize(true)
            recyclerView_safeDriving.adapter = adapter
            adapter?.notifyDataSetChanged()

            textView_safe.visibility = View.VISIBLE
            textView_unsafe.visibility = View.VISIBLE
            textView_marginal.visibility = View.VISIBLE
            textView_allVehicles_safeDriving.visibility = View.GONE

            cardView_safe.visibility = View.GONE
            cardView_unsafe.visibility = View.GONE
            cardView_marginal.visibility = View.GONE
            cardView_allVehicles_safeDriving.visibility = View.VISIBLE
        }

    }

    private fun showLoading() {
        dismissDialog()
        mDialog = this@SafeDrivingListActivity?.let {
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
        mDialog = this@SafeDrivingListActivity?.let {
            DialogHelper.getMessageDialog(it, getString(R.string.title_error), msg)
                    ?.positiveText(getString(R.string.action_ok))
                    ?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        appPreference.setAnalyticsSafeDrivingList(AppPreference.AnalyticsSafeDrivingList, ArrayList())
    }
}