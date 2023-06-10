package com.singularity.trackmyvehicle.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.databinding.ItemTripReportBinding
import com.singularity.trackmyvehicle.model.apiResponse.v3.TripReportResponse
import com.singularity.trackmyvehicle.R
import kotlinx.android.synthetic.main.item_trip_report.*

/**
 * Created by Kariba Yasmin on 11/22/21.
 */
class TripReportAdapter (
        private val context: Context,
        private var tripReportList : List<TripReportResponse.ReportSuccessResponse.ReportDataList> = ArrayList(),
        private var bstId :String = ""

        ) : RecyclerView.Adapter<TripReportAdapter.TripReportViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripReportViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_trip_report, parent, false)
        return TripReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripReportViewHolder, position: Int) {
        var tripReportData = tripReportList[position]

        holder.vrn.text = tripReportData?.carrierRegistrationNumber.toString()
        holder.bstid.text = tripReportData.getBstId()
        holder.startDate.text = tripReportData?.getTripTimeBeginCustomizeDate().toString()
        holder.endDate.text = tripReportData?.getTripTimeEndCustomizeDate().toString()
        holder.startAddress.text = tripReportData?.geoLocationNameBegin.toString()
        holder.endAddress.text = tripReportData?.geoLocationNameEnd.toString()
        holder.durationValue.text = tripReportData?.tripDurationTime.toString()
        holder.endAddress.text = tripReportData?.geoLocationNameEnd.toString()
        holder.distanceValue.text = tripReportData?.tripDistanceKm.toString()+ " " +"Km"
        holder.averageSpeed.text = tripReportData?.tripVelocityKmH.toString()+ " " +"Km/h"
    }

    override fun getItemCount(): Int {
        return tripReportList.size
    }

    class TripReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var vrn : AppCompatTextView = itemView.findViewById(R.id.textView_vrn)
        var bstid : AppCompatTextView = itemView.findViewById(R.id.item_textView_bstId)
        var startDate : AppCompatTextView = itemView.findViewById(R.id.textView_startDate)
        var endDate : AppCompatTextView = itemView.findViewById(R.id.item_textView_endDate)
        var startAddress : AppCompatTextView = itemView.findViewById(R.id.textView_startAddress)
        var endAddress : AppCompatTextView = itemView.findViewById(R.id.textView_endAddress)
        var durationValue : AppCompatTextView = itemView.findViewById(R.id.textView_duration_value)
        var distanceValue : AppCompatTextView = itemView.findViewById(R.id.textView_distance_value)
        var averageSpeed : AppCompatTextView = itemView.findViewById(R.id.textView_avg_speed_value)


    }

}