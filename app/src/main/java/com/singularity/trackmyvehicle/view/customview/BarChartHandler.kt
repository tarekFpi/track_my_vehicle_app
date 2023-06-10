package com.singularity.trackmyvehicle.view.customview

import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import com.singularity.trackmyvehicle.R
import org.joda.time.DateTime
import java.text.DecimalFormat

/**
 * Created by Sadman Sarar on 3/14/18.
 */

class BarChartHandler(barChart: BarChart,
                      private val xAxisPrefix: String,
                      private val yAxisPrefix: String,
                      private val xAxisLabelPrefix: String,
                      private val dates: ArrayList<DateTime>,
                      private val data: ArrayList<Float>,
                      private val xAxisFormatter : IAxisValueFormatter = DayAxisValueFormatter(dates),
                      private val yValueFormatter : IValueFormatter? = null,
                      private val yAxisFormatter : IAxisValueFormatter? = null,
                      private val unitInMeter : Boolean =false
) {

    private val mBarChart = barChart
    val context = barChart.context!!

    init {
        setUpBarChart()
    }

    private fun setUpBarChart() {
        mBarChart.setDrawBarShadow(false)
        mBarChart.setDrawValueAboveBar(true)

        mBarChart.description.isEnabled = false

        mBarChart.setMaxVisibleValueCount(8)
        mBarChart.setPinchZoom(false)
        mBarChart.zoom(1.0f, 1f, 0f, 0f)

        mBarChart.setDrawGridBackground(false)


        val xAxis = mBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = yAxisFormatter ?: IAxisValueFormatter { value, axis ->
            val format = DecimalFormat("##,##,##,###")

            xAxisLabelPrefix + format.format(value.toDouble())
        }

        val leftAxis = mBarChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f
        leftAxis.labelCount = 8

        mBarChart.legend.isEnabled = false
        mBarChart.axisRight.isEnabled = false


        val markerView = XYMarkerView(context, xAxisFormatter, xAxisPrefix, yAxisPrefix,unitInMeter)
        markerView.chartView = mBarChart // For bounds control
        mBarChart.marker = markerView // Set the marker to the chart

        setBarChartData()
    }

    private fun setBarChartData() {

        val yValues = ArrayList<BarEntry>()

        (0 until data.size).mapTo(yValues) {
            BarEntry(it.toFloat(), data[it] * 1f, /*ContextCompat.getDrawable(context, R.drawable.vd_plus)*/ null)
        }

        val batDataSet: BarDataSet

        if (mBarChart.data != null && mBarChart.data.dataSetCount > 0) {
            batDataSet = mBarChart.data.getDataSetByIndex(0) as BarDataSet
            batDataSet.values = yValues
            mBarChart.data.notifyDataChanged()
            mBarChart.notifyDataSetChanged()
        } else {
            batDataSet = BarDataSet(yValues, "")

            batDataSet.setDrawIcons(true)

            batDataSet.colors = ColorTemplate.VORDIPLOM_COLORS.toList();

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(batDataSet)

            val data = BarData(dataSets)
            data.setValueTextSize(12f)
            data.barWidth = 0.9f
            mBarChart.data = data
        }


        batDataSet.valueFormatter = yValueFormatter  ?: object : IValueFormatter {
            override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
                return DecimalFormat("##,##,##,### Km").format(value)
            }
        }
    }
}