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

/**
 * Created by Kariba Yasmin on 7/6/21.
 */
class AnalyticsActiveVehiclesAdapter (
        private var context: Context,
        private var activeVehiclesDataList : List<AnalyticsResponse.AnalyticsTerminal> = ArrayList()

) : RecyclerView.Adapter<AnalyticsActiveVehiclesAdapter.AnalyticsActiveVehiclesViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsActiveVehiclesViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_active_vehicle, parent, false)
        return AnalyticsActiveVehiclesViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalyticsActiveVehiclesViewHolder, position: Int) {
        var analyticsActiveVehicles = activeVehiclesDataList[position]

            holder.serialNumber.text = "${position+1}."
            holder.terminalAssignmentCode.text = analyticsActiveVehicles?.terminalAssignmentCode ?: ""
            holder.carrierRegistrationNumber.text = analyticsActiveVehicles?.carrierRegistrationNumber ?: ""

        if(position%2 == 0){
            holder.itemView.setBackgroundColor(Color.WHITE)
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#F2F2F2"))
        }
    }

    override fun getItemCount(): Int {
        return activeVehiclesDataList.size
    }

    class AnalyticsActiveVehiclesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var serialNumber : AppCompatTextView = itemView.findViewById(R.id.textView_serialNumber)
        var terminalAssignmentCode : AppCompatTextView = itemView.findViewById(R.id.textView_TerminalAssignmentCode)
        var carrierRegistrationNumber : AppCompatTextView = itemView.findViewById(R.id.textView_CarrierRegistrationNumber)

    }
}