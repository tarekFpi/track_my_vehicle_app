package com.singularity.trackmyvehicle.view.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.Vehicle
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.pager_item.view.*


class VehiclePagerViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(model: Terminal) {
        containerView.txtVehicleName.text = model.bstid
        containerView.txtVehicleDetails.text = model.vrn
    }
}

