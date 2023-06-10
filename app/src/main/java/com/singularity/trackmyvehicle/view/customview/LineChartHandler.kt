package com.singularity.trackmyvehicle.view.customview

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import com.singularity.trackmyvehicle.R
import org.joda.time.DateTime
import java.text.DecimalFormat


/**
 * Created by Sadman Sarar on 3/14/18.
 */

class LineChartHandler(linechart: LineChart,
                       private val xAxisPrefix: String,
                       private val yAxisPrefix: String,
                       private val xAxisLabelPrefix: String,
                       var values: MutableList<DateTime>,
                       var data: MutableList<Float>
) {


    private val mChart = linechart
    val context = linechart.context !!

    init {
        setUpBarChart()
    }

    fun setUpBarChart() {

        mChart.description.isEnabled = false

        mChart.setMaxVisibleValueCount(8)
        mChart.setPinchZoom(false)

        mChart.setDrawGridBackground(false)

        val xAxisFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                if(value.toInt() >= values.size) {
                    return ""
                }
                val date = values[value.toInt()]
                return date.toString("hh:mm a")
            }
        }

        val xAxis = mChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 min
        xAxis.labelCount = 3
        xAxis.enableGridDashedLine(100f, 10f, 0f);
        xAxis.valueFormatter = xAxisFormatter

        val custom = IAxisValueFormatter { value, axis ->
            val format = DecimalFormat("##,##,##,###")
            format.format(value.toDouble()) + " k/h"
        }

        val leftAxis = mChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f
        leftAxis.labelCount = 8

        mChart.axisRight.valueFormatter = custom

        mChart.legend.isEnabled = false
        mChart.axisRight.isEnabled = false
        mChart.axisLeft.isEnabled = true


        val markerView = SpeedMarkerView(context, xAxisFormatter, custom, xAxisPrefix, yAxisPrefix)
        markerView.chartView = mChart // For bounds control
        mChart.marker = markerView // Set the marker to the chart

        if (data.size != 0 && values.size != 0) {
            setBarChartData()
        }
    }

    private fun setBarChartData() {
        val yValues = ArrayList<Entry>()

        (0 until data.size).mapTo(yValues) {
            Entry(it.toFloat(), data[it] * 1f)
        }

        val lineDataSet: LineDataSet

        if (mChart.data != null && mChart.data.dataSetCount > 0) {
            lineDataSet = mChart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.values = yValues
            mChart.data.notifyDataChanged()
            mChart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(yValues, "")

            lineDataSet.setDrawIcons(true)

            lineDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(lineDataSet)

            val data = LineData(dataSets)
            data.setValueTextSize(12f)

            lineDataSet.run {
                color = Color.LTGRAY
                setCircleColor(Color.DKGRAY)
                lineWidth = 1f
                circleRadius = 3f
                setDrawCircleHole(false)
                valueTextSize = 9f
                setDrawFilled(true)
                formLineWidth = 1f
                formSize = 15f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                fillColor = ContextCompat.getColor(context, R.color.colorPrimary)
                fillAlpha = 80
            }
            mChart.data = data

            mChart.zoom(50f, 1f, 0f, 0f)
            mChart.animateY(1000, Easing.EasingOption.EaseInCubic)

            mChart.data.notifyDataChanged()
            mChart.notifyDataSetChanged()
        }


        lineDataSet.valueFormatter = object : IValueFormatter {
            override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
                return DecimalFormat("##,##,##,###").format(value)
            }
        }
    }

    fun setOffset(x: Float): Unit {
        mChart.moveViewToX(x * 1440)
    }

}