package com.singularity.trackmyvehicle.view.adapter

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setLayoutDirection
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleCurrentStatusModel
import com.singularity.trackmyvehicle.repository.interfaces.OnItemClickListener
import com.singularity.trackmyvehicle.utils.ALL_VEHICLES_TYPE
import com.singularity.trackmyvehicle.utils.setSelectedVehicleStatusColor

class VehicleCurrentStatusAdapter(
        private var context: Context,
        private var onMenuClickListener: OnItemClickListener,
        private var isHeightChange: Boolean = false

) : RecyclerView.Adapter<VehicleCurrentStatusAdapter.VehicleCurrentStatusVieHolder>() {

    private var vehicleType: Int = ALL_VEHICLES_TYPE
    private var vehicleStatusList: ArrayList<VehicleCurrentStatusModel> = ArrayList()

    fun setSelectedView(vehicleType: Int) {
        this.vehicleType = vehicleType
        notifyDataSetChanged()
    }

    fun updateVehicleList(vehicleList: ArrayList<VehicleCurrentStatusModel>) {
        vehicleStatusList = vehicleList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleCurrentStatusVieHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_vehicle_status_item, parent, false)

        val layoutParams: ViewGroup.LayoutParams = view.getLayoutParams()
        layoutParams.width = ((parent.width * .2).toInt())
        view.setLayoutParams(layoutParams)
        view.layoutDirection = View.LAYOUT_DIRECTION_LOCALE

        return VehicleCurrentStatusVieHolder(view, onMenuClickListener)
    }

    override fun onBindViewHolder(holder: VehicleCurrentStatusVieHolder, position: Int) {
        var vehicleStatusItem = vehicleStatusList[position]

        holder.vehicleCount.text = vehicleStatusItem.vehicleCount.toString()
        holder.title.text =  vehicleStatusItem.title.toString()

        Log.d("vehicleCount:","${vehicleStatusItem.vehicleCount.toString()}")

        if (!isHeightChange) {
            val params: ViewGroup.LayoutParams = holder.layoutVehicleStatusItem.layoutParams
            params.height = if (vehicleStatusItem.id == vehicleType) context.resources.getDimensionPixelSize(R.dimen.vehicle_selected_height) else context.resources.getDimensionPixelSize(R.dimen.vehicle_unselected_height)
            holder.layoutVehicleStatusItem.layoutParams = params
        }

        holder.vehicleCount.setTextColor(if (vehicleStatusItem.id == vehicleType) setSelectedVehicleStatusColor(context, vehicleType) else ContextCompat.getColor(context, R.color.darkGreyColor))
        holder.title.setTextColor(if (vehicleStatusItem.id == vehicleType) setSelectedVehicleStatusColor(context, vehicleType) else ContextCompat.getColor(context, R.color.darkGreyColor))

        holder.bottomViewVehicleStatus.visibility = if (vehicleStatusItem.id == vehicleType) View.VISIBLE else View.INVISIBLE
        holder.bottomViewVehicleStatus.setBackgroundColor(if (vehicleStatusItem.id == vehicleType) setSelectedVehicleStatusColor(context, vehicleType) else ContextCompat.getColor(context, R.color.darkGreyColor))

        holder.vehicleCount.setTypeface(if (vehicleStatusItem.id == vehicleType) Typeface.DEFAULT_BOLD else Typeface.DEFAULT)

        holder.layoutVehicleStatusItem.setBackgroundColor(if (vehicleStatusItem.id == vehicleType) Color.WHITE else ContextCompat.getColor(context, R.color.transparentWhiteColor))
    }

    override fun getItemCount(): Int {
        return vehicleStatusList.size
    }

    class VehicleCurrentStatusVieHolder(itemView: View, onMenuClickListener: OnItemClickListener)
        : RecyclerView.ViewHolder(itemView) {

        var vehicleCount: AppCompatTextView = itemView.findViewById(R.id.textView_vehicle_count)
        var title: AppCompatTextView = itemView.findViewById(R.id.textView_title)
        var bottomViewVehicleStatus: View = itemView.findViewById(R.id.bottom_view_vehicle_status)
        var layoutVehicleStatusItem: LinearLayout = itemView.findViewById(R.id.layout_item)

        init {
            itemView.setOnClickListener {
                onMenuClickListener.onItemClickListener(it, adapterPosition)
            }
        }

    }
}