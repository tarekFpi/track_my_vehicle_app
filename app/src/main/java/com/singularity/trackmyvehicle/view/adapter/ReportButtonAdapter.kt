package com.singularity.trackmyvehicle.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.ReportButtonViewHolder
import javax.inject.Inject
import javax.inject.Named

class ReportButtonAdapter(
        val callback: OnItemClickCallback<ReportButtonModel>? = null
) : RecyclerView.Adapter<ReportButtonViewHolder>() {

    init {
        VehicleTrackApplication.appComponent?.inject(this)
    }

    @Inject @field:Named("menu-buttons")
    lateinit var menuButtons: List<ReportButtonModel>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportButtonViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_button, parent, false)
        return ReportButtonViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return menuButtons.size
    }

    override fun onBindViewHolder(holder: ReportButtonViewHolder, position: Int) {
        holder.bind(menuButtons[position].label, menuButtons[position].icon)
        holder.itemView.findViewById<MaterialButton>(R.id.btnReport).setOnClickListener {
            callback?.onClick(menuButtons[holder.adapterPosition])
        }
    }
}

data class ReportButtonModel(
        val label: String,
        @DrawableRes val icon: Int,
        val slug: String = label.toLowerCase().replace(" ", "-")
)