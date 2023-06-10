package com.singularity.trackmyvehicle.utils

/**
 * Created by Sadman Sarar on 3/14/18.
 */


/**
mReportViewModel.fetchCurrentVehicleDistanceReport(mSelectedDateTime ?: DateTime.now())
.observe(this, Observer {
    data ->
    if (data?.data != null) {
        val values = data?.data
        var dates: ArrayList<DateTime> = ArrayList()
        var floats: ArrayList<Float> = ArrayList()
        values?.forEach { value ->
            dates.add(DateTime.parse(value?.date, DateTimeFormat.forPattern("yyyy-MM-dd")))
            floats.add(value?.km.toFloat())
        }

        BarChartHandler(barChartDistance,
                "Date",
                "Distance",
                "",
                dates,
                floats
        )
    }
})
mReportViewModel.fetchSpeedReport("BST216", mSelectedDateTime ?: DateTime.now())
.observe(this, Observer {
    data ->
    if (data?.data != null) {
        val values = data?.data
        var dates: ArrayList<DateTime> = ArrayList()
        var floats: ArrayList<Float> = ArrayList()
        val now = mSelectedDateTime ?: DateTime.now()

        for (i in 1 .. 31) {
            try {
                floats.add(0.0f)

                val date = now.withDayOfMonth(i)
                dates.add(date)
            } catch (ex: Exception) {
                break
            }
        }

        values?.forEach { value ->
            floats.set(DateTime(value.date).dayOfMonth, value.violations.toFloat())
        }

        BarChartHandler(barChartSpeedAlert,
                "Date",
                "Speed Alert",
                "",
                dates,
                floats
        )
    }
})
*/
