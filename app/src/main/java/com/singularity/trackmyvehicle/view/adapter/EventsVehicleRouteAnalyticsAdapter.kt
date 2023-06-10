package com.singularity.trackmyvehicle.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.apiResponse.v3.EventsVehicleRouteAnalyticsItem
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.databinding.ItemEventsVehicleRouteAnalyticsBinding

/**
 * Created by Kariba Yasmin on 8/23/21.
 */
class EventsVehicleRouteAnalyticsAdapter(
        var context : Context,
        var vehicles : List<EventsVehicleRouteAnalyticsItem> = ArrayList()

) : RecyclerView.Adapter<EventsVehicleRouteAnalyticsAdapter.EventsVehicleRouteAnalyticsViewHolder>() {

    fun vehicleList() : List<EventsVehicleRouteAnalyticsItem> {
        return vehicles
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsVehicleRouteAnalyticsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_events_vehicle_route_analytics, parent, false)
        return EventsVehicleRouteAnalyticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsVehicleRouteAnalyticsViewHolder, position: Int) {
        val item = vehicles[position]

        holder.imageView.setImageDrawable(item.modifiedItemImage(context, item.itemNumber ?: 1))
        holder.title.text = item.itemName
        holder.checkbox.isChecked = item.checked ?: false

        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            vehicles[position].checked = isChecked

        }
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }

    class EventsVehicleRouteAnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox_parking)
        var imageView : AppCompatImageView = itemView.findViewById(R.id.imageView_parking)
        var title : AppCompatTextView = itemView.findViewById(R.id.textView_parking)


    }
}