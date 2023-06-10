package com.singularity.trackmyvehicle.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.view.viewholder.SpeedReportViewHolder

/**
 * Created by Sadman Sarar on 4/19/18.
 */
class SpeedViolationAdapter : RecyclerView.Adapter<SpeedReportViewHolder>() {

    private var mData: MutableList<SpeedAlertReport>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedReportViewHolder {
        return SpeedReportViewHolder.viewHolder(parent)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun onBindViewHolder(holder: SpeedReportViewHolder, position: Int) {
        holder.bind(mData?.get(position))
    }

    fun setItems(data: MutableList<SpeedAlertReport>?) {
        mData = data
        notifyDataSetChanged()
    }
}