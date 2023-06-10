package com.singularity.trackmyvehicle.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.databinding.VehicleRouteMotionStateItemBinding

/**
 * Created by Kariba Yasmin on 8/24/21.
 */
class MotionStateVehicleRouteAnalyticsAdapter(
        private val context: Context,
        private var motionStateDataList : ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState> = ArrayList()

) : RecyclerView.Adapter<MotionStateVehicleRouteAnalyticsAdapter.MotionStateVehicleRouteAnalyticsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotionStateVehicleRouteAnalyticsViewHolder {
        var itemBinding   = DataBindingUtil.inflate<VehicleRouteMotionStateItemBinding>(LayoutInflater.from(context), R.layout.vehicle_route_motion_state_item, parent, false)
        return MotionStateVehicleRouteAnalyticsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MotionStateVehicleRouteAnalyticsViewHolder, position: Int) {
        holder.bindView(context, motionStateDataList[position])
    }

    override fun getItemCount(): Int {
        return motionStateDataList.size
    }

    class MotionStateVehicleRouteAnalyticsViewHolder( private val itemBinding: VehicleRouteMotionStateItemBinding) : RecyclerView.ViewHolder(itemBinding.root){
        fun bindView(
                context: Context,
                vehicleRouteMotionState: VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState)
        {
            itemBinding.motionStateItem = vehicleRouteMotionState
            itemBinding.executePendingBindings()
        }

    }
}