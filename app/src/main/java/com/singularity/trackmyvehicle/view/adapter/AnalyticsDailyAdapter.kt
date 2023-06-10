package com.singularity.trackmyvehicle.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kariba Yasmin on 6/1/21.
 */
class AnalyticsDailyAdapter(
        private var context: Context,
        private var analyticsDailyDataList: ArrayList<AnalyticsResponse.AnalyticsDaily> = ArrayList()

) : RecyclerView.Adapter<AnalyticsDailyAdapter.AnalyticsDailyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsDailyViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_analytics_layout, parent, false)
        return AnalyticsDailyViewHolder(view)
    }

    override fun onBindViewHolder(holderDaily: AnalyticsDailyViewHolder, position: Int) {
        var analyticsDailyData = analyticsDailyDataList[position]

        val dateToMonth = analyticsDailyData.terminalDataHourlyTimeTo?.subSequence(0, 10).toString() ?: ""
        val dateFormatPrev = SimpleDateFormat("yyyy-MM-dd")
        val d: Date = dateFormatPrev.parse(dateToMonth)
        val dateFormatForMonth = SimpleDateFormat("dd MMM yyyy")
        val dateFormatForWeek = SimpleDateFormat("EEE")

        val dateConvertToMonth: String = dateFormatForMonth.format(d)
        val dateConvertToWeek: String = dateFormatForWeek.format(d)

        var terminalDistanceKm = analyticsDailyData.terminalDistanceKilometer ?: ""
        val terminalDistanceKmDoubleNumber : Double = terminalDistanceKm.toDouble()
        val formatter = DecimalFormat("#,###.00")

        var terminalDistanceKmAfterCommaConvert = formatter.format(terminalDistanceKmDoubleNumber)

        holderDaily.dateValue.text = dateConvertToMonth
        holderDaily.dayValue.text = dateConvertToWeek
        holderDaily.vehicleCountValue.text = analyticsDailyData.terminalCount
        holderDaily.kmRunValue.text = terminalDistanceKmAfterCommaConvert.toString()

        if(position%2 == 0){
            holderDaily.itemView.setBackgroundColor(Color.WHITE)
        }else{
            holderDaily.itemView.setBackgroundColor(Color.parseColor("#F2F2F2"))
        }

    }

    override fun getItemCount(): Int {
        return analyticsDailyDataList.size
    }

    class AnalyticsDailyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var dateValue : AppCompatTextView = itemView.findViewById(R.id.textView_dateValue)
        var dayValue : AppCompatTextView = itemView.findViewById(R.id.textView_dayOrTripsValue)
        var vehicleCountValue : AppCompatTextView = itemView.findViewById(R.id.textView_vehicleCountOrSpeedingValue)
        var kmRunValue : AppCompatTextView = itemView.findViewById(R.id.textView_kmRunOrFenceOutValue)

    }

}