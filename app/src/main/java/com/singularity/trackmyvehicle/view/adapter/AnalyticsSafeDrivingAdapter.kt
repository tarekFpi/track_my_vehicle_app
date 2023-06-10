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
import com.singularity.trackmyvehicle.utils.Log

/**
 * Created by Kariba Yasmin on 7/25/21.
 */
class AnalyticsSafeDrivingAdapter (
        var context: Context,
        var safeDrivingDataList : List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank> = ArrayList(),
        var safeDrivingBstidVrnDataList : List<AnalyticsResponse.AnalyticsTerminal> = ArrayList()

) : RecyclerView.Adapter<AnalyticsSafeDrivingAdapter.AnalyticsSafeDrivingViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsSafeDrivingViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_safe_driving, parent, false)
        return AnalyticsSafeDrivingViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsSafeDrivingViewHolder, position: Int) {

        // if(safeDrivingBstidVrnDataList[position].terminalAssignmentIsSuspended.toString().contains("0")){

             var analyticsSafeDriving = safeDrivingDataList[position]
             var safeDrivingData = safeDrivingBstidVrnDataList[position]

             holder.serialNumber.text = "${position+1}."
             holder.terminalAssignmentCode.text = "t"+safeDrivingData.terminalAssignmentCode
             holder.carrierRegistrationNumber.text = safeDrivingData.carrierRegistrationNumber
             holder.totalAlerts.text = analyticsSafeDriving.modifiedTotalAlerts().toString()

             if(position%2 == 0){
                 holder.itemView.setBackgroundColor(Color.WHITE)
             }else{
                 holder.itemView.setBackgroundColor(Color.parseColor("#F2F2F2"))
             }

      //     Log.d("terminalAssignmentIsSuspended:","${safeDrivingData.terminalAssignmentIsSuspended}")

    }

    override fun getItemCount(): Int {
        return safeDrivingDataList.size
    }

    class AnalyticsSafeDrivingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var serialNumber : AppCompatTextView = itemView.findViewById(R.id.textView_serialNumber)
        var terminalAssignmentCode : AppCompatTextView = itemView.findViewById(R.id.textView_TerminalAssignmentCode)
        var carrierRegistrationNumber : AppCompatTextView = itemView.findViewById(R.id.textView_CarrierRegistrationNumber)
        var totalAlerts : AppCompatTextView = itemView.findViewById(R.id.textView_TotalAlerts)

    }
}