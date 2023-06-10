package com.singularity.trackmyvehicle.view.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.adapter.AnalyticsDailyAdapter
import com.singularity.trackmyvehicle.view.adapter.AnalyticsEventAdapter
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import kotlinx.android.synthetic.main.fragment_analytics.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.ceil


class AnalyticsFragment : Fragment() {

    @Inject
    lateinit var mPrefRepository: PrefRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mDialog: MaterialDialog? = null

    val script: String = "API/V1/Analytics"

    var analyticsDailyDataList: ArrayList<AnalyticsResponse.AnalyticsDaily> = ArrayList()

    var analyticsEventDataList: ArrayList<AnalyticsResponse.AnalyticsEvent> = ArrayList()

    var vehicle: ArrayList<PieEntry> = ArrayList()
    var pieDataSet: PieDataSet = PieDataSet(vehicle, "")

    var cookie: String = ""

    var userName: String = ""

    private lateinit var appPreference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VehicleTrackApplication.appComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cookie = mPrefRepository.cookie
        userName = mPrefRepository.userName()

        appPreference = activity?.let { AppPreferenceImpl(it) }!!

        setReportTab()

        setGraphTab()

        setCustomTextColor()

        getAnalyticsResponse()

    }

    private fun switchToActiveSafeListActivity(analyticsSafeDriving: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>, analyticsTerminal: List<AnalyticsResponse.AnalyticsTerminal>) {

        layout_activeVehicleList.setOnClickListener {
            appPreference.setAnalyticsTerminalList(AppPreference.AnalyticsTerminalList, analyticsTerminal)

            var intent = Intent(activity, ActiveVehicleListActivity::class.java)
            startActivity(intent)
        }

        layout_safeDrivingList.setOnClickListener {
            appPreference.setAnalyticsTerminalList(AppPreference.AnalyticsTerminalList, analyticsTerminal)
            appPreference.setAnalyticsSafeDrivingList(AppPreference.AnalyticsSafeDrivingList, analyticsSafeDriving)

            var intent = Intent(activity, SafeDrivingListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAnalyticsResponse() {

        showLoading()

        val call = RetrofitClient.getInstance(cookie)
                ?.create(ApiHandler::class.java)
                ?.getAnalyticsResponse(script)

        call?.enqueue(object : Callback<AnalyticsResponse> {
            override fun onFailure(call: Call<AnalyticsResponse>, t: Throwable) {
                if (!activity?.isDestroyed!!) dismissDialog()
                showDialogError("Something went wrong, Please try again")
            }

            override fun onResponse(call: Call<AnalyticsResponse>, response: Response<AnalyticsResponse>) {
                if (!activity?.isDestroyed!!) dismissDialog()
                if (response.isSuccessful && response.body() != null && response.body()?.analyticsErrorResponse?.analyticsErrorCode == 0) {


                    getAnalyticsDailyData(response.body()?.analyticsSuccessResponse?.analyticsDaily
                            ?: ArrayList())

                    getAnalyticsEventData(response.body()?.analyticsSuccessResponse?.analyticsEvent
                            ?: ArrayList())

                    getActiveAndSafeVehicles(response.body()?.analyticsSuccessResponse?.analyticsTerminal
                            ?: ArrayList(), response.body()?.analyticsSuccessResponse?.analyticsDriveSafetyRank?.rank
                            ?: ArrayList())

                    //Log.e("jhg", Gson().toJson(response.body()?.analyticsSuccessResponse))

                    switchToActiveSafeListActivity(response.body()?.analyticsSuccessResponse?.analyticsDriveSafetyRank?.rank
                            ?: ArrayList(), response.body()?.analyticsSuccessResponse?.analyticsTerminal
                            ?: ArrayList())

                    setCurrentUserName(response.body()?.analyticsUserResponse?.analyticsUserName
                            ?: "")
                    getHowManyDaysSummary(response.body()?.analyticsSuccessResponse?.parameter?.days
                            ?: "")

                    getTotalKm(response.body()?.analyticsSuccessResponse?.analyticsDaily
                            ?: ArrayList())

                    getTotalAlertSummary(response.body()?.analyticsSuccessResponse?.analyticsEvent
                            ?: ArrayList())

                    setVehicleBarChart((response.body()?.analyticsSuccessResponse?.analyticsDaily
                            ?: ArrayList()).reversed())

                    setTraveledKmLineChart((response.body()?.analyticsSuccessResponse?.analyticsDaily
                            ?: ArrayList()).reversed())

                    setVehicleTripsMadeLineChart((response.body()?.analyticsSuccessResponse?.analyticsEvent
                            ?: ArrayList()).reversed())

                }
            }
        })
    }

    private fun getActiveAndSafeVehicles(list: List<AnalyticsResponse.AnalyticsTerminal>, list1: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>) {
        var activeVehicleCount = 0
        var offlineVehicleCount = 0
        var suspendedVehicleCount = 0
        var safeDrivingCount = 0

        for (item in list) {
            if (item.terminalState == "Live") {
                activeVehicleCount++
            } else if (item.terminalState == "Offline") {
                offlineVehicleCount++
            } else if (item.terminalState == "Suspended") {
                suspendedVehicleCount++
            }

        }

        for (perItem in list1) {
            if (perItem.driveSafetyOrder == "1") {
                safeDrivingCount++
            }
        }

        var totalVehicle = 0
        var activeVehicleRatio = 0
        var safeDrivingRatio = 0

        if (activeVehicleCount != 0 || offlineVehicleCount != 0 || suspendedVehicleCount != 0) {
            totalVehicle = activeVehicleCount + offlineVehicleCount + suspendedVehicleCount
            activeVehicleRatio = ceil((activeVehicleCount.toDouble() / totalVehicle.toDouble()) * 100).toInt()

            safeDrivingRatio = ceil((safeDrivingCount.toDouble() / totalVehicle.toDouble()) * 100).toInt()
        }


        val formatter = DecimalFormat("#,###,###")

        var totalVehicleAfterConvert = formatter.format(totalVehicle)
        var activeVehicleAfterConvert = formatter.format(activeVehicleCount)
        var safeDrivingAfterConvert = formatter.format(safeDrivingCount)

        textView_activeVehicleNumber.text = activeVehicleAfterConvert.toString()
        textView_safeDrivingNumber.text = safeDrivingAfterConvert.toString()

        textView_activeVehiclesPercentage.text = "${activeVehicleRatio}%"
        textView_safeVehiclesPercentage.text = "${safeDrivingRatio}%"

        waveView_activeVehicles.progress = activeVehicleRatio.toInt()
        waveView_safeDriving.progress = safeDrivingRatio.toInt()

        waveView_activeVehicles.setAnimationSpeed(60)
        waveView_safeDriving.setAnimationSpeed(60)

        val totalVehicles = getString(R.string.total_vehicles_number)
        val vehicleNumber = "<b><font color='${resources.getColor(R.color.black)}'> $totalVehicleAfterConvert </font></b>"

        textView_totalVehicles.text = Html.fromHtml(totalVehicles + " " + vehicleNumber)

    }

    private fun getHowManyDaysSummary(summaryDays: String) {
        val last = getString(R.string.last)
        val sevenDays = "<b><font color='${resources.getColor(R.color.colorPrimary)}'> $summaryDays days </font></b>"
        val summary = getString(R.string.summary)

        textView_seven_days_summary_details.text = Html.fromHtml(last + sevenDays + summary)

    }

    private fun setCustomTextColor() {
        val your = getString(R.string.your)
        val week = "<b><font color='#ffffff'> ${getString(R.string.week)} </font></b>"
        val inNumbers = getString(R.string.in_numbers)

        textView_weekInNumbers.text = Html.fromHtml(your + week + inNumbers)

        textView_userName.text = "$userName!"
    }

    private fun setCurrentUserName(userName: String) {
        textView_userName.text = ("$userName!")

    }

    private fun setReportTab() {
        textView_reports.setOnClickListener {
            view_separatorReports.visibility = View.VISIBLE
            view_separatorGraphs.visibility = View.GONE
            layout_reportDetails.visibility = View.VISIBLE
            layout_graphDetails.visibility = View.GONE

            textView_reports.setTypeface(null, Typeface.BOLD)
            textView_graphs.setTypeface(null, Typeface.NORMAL)

            guideline_first_user_layer.setGuidelinePercent(.12f)
            guideline_below_userName.setGuidelinePercent(.06f)
        }
    }

    private fun setGraphTab() {
        textView_graphs.setOnClickListener {
            view_separatorReports.visibility = View.GONE
            view_separatorGraphs.visibility = View.VISIBLE
            layout_reportDetails.visibility = View.GONE
            layout_graphDetails.visibility = View.VISIBLE

            textView_reports.setTypeface(null, Typeface.NORMAL)
            textView_graphs.setTypeface(null, Typeface.BOLD)

            guideline_below_userName.setGuidelinePercent(.042f)
            guideline_first_user_layer.setGuidelinePercent(.088f)
        }
    }

    private fun getAnalyticsDailyData(analyticDailyData: List<AnalyticsResponse.AnalyticsDaily>) {
        analyticsDailyDataList.clear()
        analyticsDailyDataList.addAll(analyticDailyData)
        var adapter = activity?.let { AnalyticsDailyAdapter(it, analyticsDailyDataList) }
        recyclerView_traveled_distance.isNestedScrollingEnabled = false
        recyclerView_traveled_distance.setHasFixedSize(true)
        recyclerView_traveled_distance.adapter = adapter
        adapter?.notifyDataSetChanged()

    }

    private fun getAnalyticsEventData(analyticEventData: List<AnalyticsResponse.AnalyticsEvent>) {
        analyticsEventDataList.clear()
        analyticsEventDataList.addAll(analyticEventData)
        var adapter = activity?.let { AnalyticsEventAdapter(it, analyticsEventDataList) }
        recyclerView_summary_alert.isNestedScrollingEnabled = false
        recyclerView_summary_alert.setHasFixedSize(true)
        recyclerView_summary_alert.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun getTotalKm(list: List<AnalyticsResponse.AnalyticsDaily>) {
        var vehicleKmList: ArrayList<Double> = ArrayList()

        for (item in list) {

            vehicleKmList.add(item.terminalDistanceKilometer?.toDouble() ?: 0.0)
        }

        var totalVehicleKm = vehicleKmList.sum()

        val formatter = DecimalFormat("#,###.##")

        var totalVehicleKmAfterConvert = formatter.format(totalVehicleKm)

        var kmInTotal: String = getString(R.string.km_in_total)

        textView_kmInTotal.text = ("$totalVehicleKmAfterConvert $kmInTotal")

        textView_kmTraveled_count.text = totalVehicleKmAfterConvert

    }

    private fun getTotalAlertSummary(list: List<AnalyticsResponse.AnalyticsEvent>) {
        var totalTripsList: ArrayList<Int> = ArrayList()

        var totalSpeedingList: ArrayList<Int> = ArrayList()

        var totalFenceOutList: ArrayList<Int> = ArrayList()

        for (item in list) {

            totalTripsList.add(item.tripCount?.toInt() ?: 0)
            totalSpeedingList.add(item.overspeedCount?.toInt() ?: 0)
            totalFenceOutList.add(item.geofenceExitCount?.toInt() ?: 0)
        }

        var totalTrips = totalTripsList.sum()
        var totalSpeed = totalSpeedingList.sum()
        var totalFenceOut = totalFenceOutList.sum()

        val formatter = DecimalFormat("#,###,###")

        var totalTripsAfterConvert = formatter.format(totalTrips)
        var totalSpeedAfterConvert = formatter.format(totalSpeed)
        var totalFenceOutAfterConvert = formatter.format(totalFenceOut)

        textView_summaryAlerts_subtitle.text = ("$totalTripsAfterConvert ${getString(R.string.trips_alert)} " +
                ". $totalSpeedAfterConvert ${getString(R.string.speeding_alert)} . $totalFenceOutAfterConvert ${getString(R.string.fence_out_alert)}")

        textView_trips_count.text = totalTripsAfterConvert
        textView_fenceOut_count.text = totalFenceOutAfterConvert
        textView_overSpeed_count.text = totalSpeedAfterConvert

    }

    private fun setVehicleBarChart(list: List<AnalyticsResponse.AnalyticsDaily>) {

        var dailyResponse: ArrayList<String> = ArrayList()

        for (dateTo in list) {
            val dateToMonth = dateTo.terminalDataHourlyTimeTo?.subSequence(0, 10).toString() ?: ""
            val dateFormatPrev = SimpleDateFormat("yyyy-MM-dd")
            val d: Date = dateFormatPrev.parse(dateToMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val changedDate: String = dateFormat.format(d)

            //dailyResponse.add(dateTo.terminalDataHourlyTimeTo?.subSequence(0, 10).toString() ?: "")
            dailyResponse.add(changedDate ?: "")

        }

        vehicleBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyResponse)
        vehicleBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        vehicleBarChart.xAxis.labelRotationAngle = -40F
        vehicleBarChart.xAxis.setDrawGridLines(false)

        vehicleBarChart.setDrawGridBackground(false)
        vehicleBarChart.axisLeft.isEnabled = true
        vehicleBarChart.axisLeft.setDrawAxisLine(false)
        vehicleBarChart.axisLeft.setDrawGridLines(true)
        vehicleBarChart.axisLeft.enableGridDashedLine(10f, 10f, 0f)

        vehicleBarChart.axisRight.isEnabled = false
        vehicleBarChart.axisRight.setDrawGridLines(false)

        vehicleBarChart.description.isEnabled = false
        vehicleBarChart.legend.isEnabled = false

        var barChart: ArrayList<BarEntry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            Log.e("jyhg", "entered 2")
            barChart.add(BarEntry(index.toFloat(), value.terminalCount?.toFloat() ?: 0.0F))
        }
        Log.e("jyhg", "entered 3")


        var barDataSet: BarDataSet = BarDataSet(barChart, "")
        barDataSet.setColors(resources.getColor(R.color.barChartColorFill))
        barDataSet.valueTextColor = resources.getColor(R.color.barChartColorFill)
        barDataSet.valueTextSize = 0f

        var barData: BarData = BarData(barDataSet)

        vehicleBarChart.invalidate()
        vehicleBarChart.data = barData
        vehicleBarChart.animateY(2000)

        barData.barWidth = 0.2f

    }

    private fun setTraveledKmLineChart(list: List<AnalyticsResponse.AnalyticsDaily>) {
        var dailyResponse: ArrayList<String> = ArrayList()

        for (dateTo in list) {
            val dateToMonth = dateTo.terminalDataHourlyTimeTo?.subSequence(0, 10).toString() ?: ""
            val dateFormatPrev = SimpleDateFormat("yyyy-MM-dd")
            val d: Date = dateFormatPrev.parse(dateToMonth)
            //val dateFormat = SimpleDateFormat("EEE dd MMM yyyy")
            val dateFormat = SimpleDateFormat("dd MMM")
            val changedDate: String = dateFormat.format(d)

            // dailyResponse.add(dateTo.terminalDataHourlyTimeTo?.subSequence(0, 10).toString() ?: "")
            dailyResponse.add(changedDate ?: "")
        }

        traveledKmLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyResponse)
        traveledKmLineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        traveledKmLineChart.xAxis.labelRotationAngle = -40F
        traveledKmLineChart.xAxis.setDrawGridLines(false)

        traveledKmLineChart.setDrawBorders(false)
        traveledKmLineChart.setDrawGridBackground(false)

        traveledKmLineChart.axisLeft.isEnabled = true
        traveledKmLineChart.axisLeft.setDrawAxisLine(false)
        traveledKmLineChart.axisLeft.setDrawGridLines(true)
        traveledKmLineChart.axisLeft.enableGridDashedLine(10f, 10f, 0f)

        traveledKmLineChart.axisRight.isEnabled = false
        traveledKmLineChart.axisRight.setDrawGridLines(false)

        traveledKmLineChart.description.isEnabled = false
        traveledKmLineChart.legend.isEnabled = false
        traveledKmLineChart.setExtraOffsets(0f, 0f, 28f, 10f)

        var lineChart: ArrayList<Entry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            lineChart.add(Entry(index.toFloat(), value.terminalDistanceKilometer?.toDouble()?.toFloat()
                    ?: 0.0f))
        }

        val lineDataSet = LineDataSet(lineChart, "Line Chart")

        lineDataSet.color = resources.getColor(R.color.lineChartColorFill)
        lineDataSet.setCircleColor(resources.getColor(R.color.lineChartColorFill))
        //lineDataSet.setCircleColorHole(resources.getColor(R.color.lineChartColorFill))
        lineDataSet.circleRadius = 5.4f
        lineDataSet.lineWidth = 2f
        lineDataSet.valueTextSize = 12f
        lineDataSet.valueTextColor = resources.getColor(R.color.lineChartValueTextColor)
        lineDataSet.circleHoleRadius = 3.4F
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawFilled(true)
        val fillGradient = ContextCompat.getDrawable(requireContext(), R.drawable.analytics_linechart_gradient)
        lineDataSet.fillDrawable = fillGradient
        //lineDataSet.fillColor = resources.getColor(R.color.lineChartColorFill)


        var lineData = LineData()
        if (lineChart.isEmpty()) {
            lineChart.clear()
        } else {
            lineData = LineData(lineDataSet)
        }
        //val lineData = LineData(lineDataSet)

        traveledKmLineChart.invalidate()
        traveledKmLineChart.data = lineData
        traveledKmLineChart.animateXY(3000, 3000)

    }

    private fun setVehicleTripsMadeLineChart(list: List<AnalyticsResponse.AnalyticsEvent>) {

        var eventResponse: ArrayList<String> = ArrayList()

        for (dateTo in list) {
            val dateToMonth = dateTo.eventDate?.subSequence(0, 10).toString() ?: ""
            val dateFormatPrev = SimpleDateFormat("yyyy-MM-dd")
            val d: Date = dateFormatPrev.parse(dateToMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val changedDate: String = dateFormat.format(d)

            //eventResponse.add(dateTo.eventDate?.subSequence(0, 10).toString() ?: "")
            eventResponse.add(changedDate ?: "")
        }

        tripsMadeLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(eventResponse)
        tripsMadeLineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        tripsMadeLineChart.xAxis.labelRotationAngle = -40F
        tripsMadeLineChart.xAxis.setDrawGridLines(false)

        tripsMadeLineChart.setDrawGridBackground(false)
        tripsMadeLineChart.axisLeft.isEnabled = true
        tripsMadeLineChart.axisLeft.setDrawAxisLine(false)
        tripsMadeLineChart.axisLeft.setDrawGridLines(true)
        tripsMadeLineChart.axisLeft.enableGridDashedLine(10f, 10f, 0f)

        tripsMadeLineChart.axisRight.isEnabled = false
        tripsMadeLineChart.axisRight.setDrawGridLines(false)

        tripsMadeLineChart.description.isEnabled = false
        tripsMadeLineChart.legend.isEnabled = false
        tripsMadeLineChart.setExtraOffsets(0f, 0f, 28f, 10f)

        var lineChart: ArrayList<Entry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            lineChart.add(Entry(index.toFloat(), value.tripCount?.toFloat() ?: 0.0f))
        }

        val lineDataSet = LineDataSet(lineChart, "Line Chart")

        lineDataSet.color = resources.getColor(R.color.lineChartTripsMadeColor)
        lineDataSet.setCircleColor(resources.getColor(R.color.lineChartTripsMadeColor))
        lineDataSet.setCircleColorHole(resources.getColor(R.color.lineChartTripsMadeColor))
        lineDataSet.circleRadius = 4f
        lineDataSet.lineWidth = 2f
        lineDataSet.valueTextSize = 0f
        lineDataSet.circleHoleRadius = 0F
        //lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        var lineData = LineData()
        if (lineChart.isEmpty()) {
            lineChart.clear()
        } else {
            lineData = LineData(lineDataSet)
        }
        //val lineData = LineData(lineDataSet)

        tripsMadeLineChart.invalidate()
        tripsMadeLineChart.data = lineData
        tripsMadeLineChart.animateXY(3000, 3000)

    }

    private fun showLoading() {
        dismissDialog()
        mDialog = activity?.let {
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
        mDialog = activity?.let {
            DialogHelper.getMessageDialog(it, getString(R.string.title_error), msg)
                    ?.positiveText(getString(R.string.action_ok))
                    ?.show()
        }
    }

}