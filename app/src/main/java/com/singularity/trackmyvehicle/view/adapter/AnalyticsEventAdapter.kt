package com.singularity.trackmyvehicle.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import com.singularity.trackmyvehicle.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kariba Yasmin on 6/2/21.
 */
class AnalyticsEventAdapter(
        private var context: Context,
        private var analyticsEventDataList : ArrayList<AnalyticsResponse.AnalyticsEvent> = ArrayList()

) : RecyclerView.Adapter<AnalyticsEventAdapter.AnalyticsEventViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsEventViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_analytics_layout, parent, false)

        return AnalyticsEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsEventViewHolder, position: Int) {
        var analyticsEventData = analyticsEventDataList[position]

        val dateToMonth = analyticsEventData.eventDate?.subSequence(0, 10).toString() ?: ""
        val dateFormatPrev = SimpleDateFormat("yyyy-MM-dd")
        val d: Date = dateFormatPrev.parse(dateToMonth)
        val dateFormatForMonth = SimpleDateFormat("dd MMM yyyy")

        val dateConvertToMonth: String = dateFormatForMonth.format(d)

        var totalTrips = analyticsEventData.tripCount?.toInt() ?: 0
        val formatter = DecimalFormat("#,###,###")

        var totalTripsAfterConvert = formatter.format(totalTrips)

        holder.dateValue.text = dateConvertToMonth
        holder.tripsValue.text = totalTripsAfterConvert
        holder.speedingValue.text = analyticsEventData.overspeedCount
        holder.fenceOutValue.text = analyticsEventData.geofenceExitCount

        if(position%2 == 0){
            holder.itemView.setBackgroundColor(Color.WHITE)
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#F2F2F2"))
        }
    }

    override fun getItemCount(): Int {
        return analyticsEventDataList.size
    }

    class AnalyticsEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var dateValue : AppCompatTextView = itemView.findViewById(R.id.textView_dateValue)
        var tripsValue: AppCompatTextView = itemView.findViewById(R.id.textView_dayOrTripsValue)
        var speedingValue : AppCompatTextView = itemView.findViewById(R.id.textView_vehicleCountOrSpeedingValue)
        var fenceOutValue : AppCompatTextView = itemView.findViewById(R.id.textView_kmRunOrFenceOutValue)

    }
}