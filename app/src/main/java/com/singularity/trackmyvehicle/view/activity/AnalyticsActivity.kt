package com.singularity.trackmyvehicle.view.activity

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.adapter.AnalyticsDailyAdapter
import com.singularity.trackmyvehicle.view.adapter.AnalyticsEventAdapter
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_analytics.*
import kotlinx.android.synthetic.main.activity_virtual_watchman_set.*
import kotlinx.android.synthetic.main.fragment_feedback_list.*
import kotlinx.android.synthetic.main.item_engine_pie_chart.*
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class AnalyticsActivity : BaseActivity() {


    @Inject lateinit var mPrefRepository: PrefRepository

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mDialog: MaterialDialog? = null

    val script : String = "API/V1/Analytics"

    var analyticsDailyDataList : ArrayList<AnalyticsResponse.AnalyticsDaily> = ArrayList()

    var analyticsEventDataList : ArrayList<AnalyticsResponse.AnalyticsEvent> = ArrayList()

    var vehicle : ArrayList<PieEntry> = ArrayList()
    var pieDataSet : PieDataSet = PieDataSet(vehicle, "")

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_analytics)
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        getAnalyticsResponse()

    }

    private fun getAnalyticsResponse() {

        showLoading()

        val call = RetrofitClient.getInstance(mPrefRepository.cookie)
                ?.create(ApiHandler::class.java)
                ?.getAnalyticsResponse(script)


        call?.enqueue(object : Callback<AnalyticsResponse> {
            override fun onFailure(call: Call<AnalyticsResponse>, t: Throwable) {
                if (! this@AnalyticsActivity.isDestroyed) dismissDialog()
                showDialogError("Something went wrong, Please try again")
            }

            override fun onResponse(call: Call<AnalyticsResponse>, response: Response<AnalyticsResponse>) {
                if (! this@AnalyticsActivity.isDestroyed) dismissDialog()
                if (response.isSuccessful && response.body() != null && response.body()?.analyticsErrorResponse?.analyticsErrorCode == 0) {


                    /*getAnalyticsDailyData(response.body()?.analyticsSuccessResponse?.analyticsDaily
                            ?: ArrayList())

                    getAnalyticsEventData(response.body()?.analyticsSuccessResponse?.analyticsEvent
                            ?: ArrayList())

                    getStartAndEndDate(response.body()?.analyticsSuccessResponse?.parameter?.timeFrom
                            ?: "", response.body()?.analyticsSuccessResponse?.parameter?.timeTo !!)

                    getTotalVehicle(response.body()?.analyticsSuccessResponse?.analyticsStatus ?: ArrayList())

                    setTotalVehiclePieChart(response.body()?.analyticsSuccessResponse?.analyticsStatus ?: ArrayList())

                    setVehicleBarChart((response.body()?.analyticsSuccessResponse?.analyticsDaily ?: ArrayList()).reversed())

                    setTraveledKmLineChart((response.body()?.analyticsSuccessResponse?.analyticsDaily ?: ArrayList()).reversed())

                    setVehicleTripsMadeLineChart((response.body()?.analyticsSuccessResponse?.analyticsEvent ?: ArrayList()).reversed())*/

                }
            }
        })
    }

    /*private fun getAnalyticsDailyData(analyticDailyData: List<AnalyticsResponse.AnalyticsDaily>){
        analyticsDailyDataList.clear()
        analyticsDailyDataList.addAll(analyticDailyData)
        var adapter = AnalyticsDailyAdapter(this@AnalyticsActivity, analyticsDailyDataList)
        recyclerView_traveled_distance.isNestedScrollingEnabled = false
        recyclerView_traveled_distance.setHasFixedSize(true)
        recyclerView_traveled_distance.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    private fun getAnalyticsEventData(analyticEventData: List<AnalyticsResponse.AnalyticsEvent>){
        analyticsEventDataList.clear()
        analyticsEventDataList.addAll(analyticEventData)
        var adapter = AnalyticsEventAdapter(this@AnalyticsActivity, analyticsEventDataList)
        recyclerView_summary_alert.isNestedScrollingEnabled = false
        recyclerView_summary_alert.setHasFixedSize(true)
        recyclerView_summary_alert.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun getStartAndEndDate(startDate: String, endDate: String) {
        textView_dateStart.text = startDate.subSequence(0, 10)
        textView_dateEnd.text = endDate.subSequence(0, 10)
    }

    private fun getTotalVehicle(list: List<AnalyticsResponse.AnalyticsStatus>) {
        var vehicleList : ArrayList<Int> = ArrayList()

        for(item in list){

            vehicleList.add(item.terminalCount?.toInt() ?: 0)
        }

        var totalVehicleCount = vehicleList.sum()

        var totalText : String = getString(R.string.total)
        var vehiclesText : String = getString(R.string.vehicles)
        textView_totalVehicle_title.text = ("$totalText  $totalVehicleCount $vehiclesText")

    }

    private fun setTotalVehiclePieChart(list: List<AnalyticsResponse.AnalyticsStatus>) {
        var colorListInteger : ArrayList<Int> = ArrayList()

        for(item in list){
            vehicle.add(PieEntry(item.terminalCount?.toFloat() ?: 0.0F, item.terminalState))

            colorListInteger.add(Color.parseColor(item.terminalStateColor ?: "#000000"))
        }
        pieDataSet.setColors(colorListInteger)

        totalVehiclePieChart.isDrawHoleEnabled = true
        totalVehiclePieChart.setHoleColor(resources.getColor(R.color.demo_light_transparent))
        totalVehiclePieChart.holeRadius = 0F
        totalVehiclePieChart.transparentCircleRadius = 1F

        pieDataSet.valueTextColor = resources.getColor(R.color.white)
        pieDataSet.valueTextSize = 14f

        var pieData : PieData = PieData(pieDataSet)

        totalVehiclePieChart.data = pieData
        totalVehiclePieChart.description.isEnabled = false
        totalVehiclePieChart.animate()
        totalVehiclePieChart.setDrawSliceText(false)
        totalVehiclePieChart.setTouchEnabled(true)
        totalVehiclePieChart.invalidate()

        var l : Legend = totalVehiclePieChart.legend
        l.position = Legend.LegendPosition.BELOW_CHART_CENTER
        l.textSize = 12f
        l.form = Legend.LegendForm.CIRCLE
        l.formToTextSpace = 2f
        l.stackSpace = 1f
        l.formSize = 10f

    }

    private fun setVehicleBarChart(list: List<AnalyticsResponse.AnalyticsDaily>) {

        var dailyResponse : ArrayList<String> = ArrayList()

        for(dateTo in list){
            dailyResponse.add(dateTo.terminalDataHourlyTimeTo?.subSequence(0,10).toString() ?: "")
        }

        vehicleBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyResponse)
        vehicleBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        vehicleBarChart.xAxis.labelRotationAngle = -40F

        vehicleBarChart.setDrawGridBackground(false)
        vehicleBarChart.axisLeft.isEnabled = true
        vehicleBarChart.axisRight.isEnabled = false
        vehicleBarChart.description.isEnabled = false
        vehicleBarChart.legend.isEnabled = false

        var barChart : ArrayList<BarEntry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            barChart.add(BarEntry(index.toFloat(), value.terminalCount?.toFloat() ?: 0.0F))
        }


        var barDataSet : BarDataSet = BarDataSet(barChart, "")
        barDataSet.setColors(resources.getColor(R.color.barChartColorOne),
                resources.getColor(R.color.barChartColorTwo),
                resources.getColor(R.color.barChartColorThree),
                resources.getColor(R.color.barChartColorFour),
                resources.getColor(R.color.barChartColorFive),
                resources.getColor(R.color.barChartColorSix),
                resources.getColor(R.color.barChartColorSeven))
        barDataSet.valueTextColor = resources.getColor(R.color.barChartColorSix)
        barDataSet.valueTextSize = 16f

        var barData : BarData = BarData(barDataSet)

        vehicleBarChart.invalidate()
        vehicleBarChart.data = barData
        vehicleBarChart.animateY(2000)

    }

    private fun setTraveledKmLineChart(list : List<AnalyticsResponse.AnalyticsDaily>) {
        var dailyResponse : ArrayList<String> = ArrayList()

        for(dateTo in list){
            dailyResponse.add(dateTo.terminalDataHourlyTimeTo?.subSequence(0,10).toString() ?: "")
        }

        traveledKmLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyResponse)
        traveledKmLineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        traveledKmLineChart.xAxis.labelRotationAngle = -40F

        traveledKmLineChart.setDrawGridBackground(false)
        traveledKmLineChart.axisLeft.isEnabled = true
        traveledKmLineChart.axisRight.isEnabled = false
        traveledKmLineChart.description.isEnabled = false
        traveledKmLineChart.legend.isEnabled = false
        traveledKmLineChart.setExtraOffsets(0f,0f,28f,10f)

        var lineChart : ArrayList<Entry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            lineChart.add(Entry(index.toFloat(), value.terminalDistanceKilometer?.toDouble()?.toFloat() ?: 0.0f))
        }

        val lineDataSet = LineDataSet(lineChart, "Line Chart")

        lineDataSet.color = resources.getColor(R.color.lineChartColorFill)
        lineDataSet.setCircleColor(resources.getColor(R.color.lineChartColorFill))
        lineDataSet.setCircleColorHole(resources.getColor(R.color.lineChartColorFill))
        lineDataSet.circleRadius = 4f
        lineDataSet.lineWidth = 2f
        lineDataSet.valueTextSize = 12f
        lineDataSet.circleHoleRadius = 0F
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillColor = resources.getColor(R.color.lineChartColorFill)

        val lineData = LineData(lineDataSet)

        traveledKmLineChart.invalidate()
        traveledKmLineChart.data = lineData
        traveledKmLineChart.animateXY(3000, 3000)

    }

    private fun setVehicleTripsMadeLineChart(list : List<AnalyticsResponse.AnalyticsEvent>) {

        var eventResponse : ArrayList<String> = ArrayList()

        for(dateTo in list){
            eventResponse.add(dateTo.eventDate?.subSequence(0,10).toString() ?: "")
        }

        tripsMadeLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(eventResponse)
        tripsMadeLineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        tripsMadeLineChart.xAxis.labelRotationAngle = -40F

        tripsMadeLineChart.setDrawGridBackground(false)
        tripsMadeLineChart.axisLeft.isEnabled = true
        tripsMadeLineChart.axisRight.isEnabled = false
        tripsMadeLineChart.description.isEnabled = false
        tripsMadeLineChart.legend.isEnabled = false
        tripsMadeLineChart.setExtraOffsets(0f,0f,28f,10f)

        var lineChart : ArrayList<Entry> = ArrayList()

        for ((index, value) in list.withIndex()) {
            lineChart.add(Entry(index.toFloat(), value.tripCount?.toFloat() ?: 0.0f))
        }

        val lineDataSet = LineDataSet(lineChart, "Line Chart")

        lineDataSet.color = resources.getColor(R.color.lineChartTripsMadeColor)
        lineDataSet.setCircleColor(resources.getColor(R.color.lineChartTripsMadeColor))
        lineDataSet.setCircleColorHole(resources.getColor(R.color.lineChartTripsMadeColor))
        lineDataSet.circleRadius = 4f
        lineDataSet.lineWidth = 2f
        lineDataSet.valueTextSize = 12f
        lineDataSet.circleHoleRadius = 0F
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(lineDataSet)

        tripsMadeLineChart.invalidate()
        tripsMadeLineChart.data = lineData
        tripsMadeLineChart.animateXY(3000, 3000)

    }*/

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