package com.singularity.trackmyvehicle.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.VehiclePagerViewHolder


class VehiclePagerAdapter(
        private val itemCallback:  OnItemClickCallback<Terminal>? = null
) : RecyclerView.Adapter<VehiclePagerViewHolder>() {

    var mData: List<Terminal>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehiclePagerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.pager_item, parent, false)
        return VehiclePagerViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun onBindViewHolder(holder: VehiclePagerViewHolder, position: Int) {
        mData?.get(holder.adapterPosition)?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            mData?.get(holder.adapterPosition)?.let {
                it1 -> itemCallback?.onClick(it1,holder.adapterPosition)
            }
        }
    }
    fun setData(data: List<Terminal>) {
        mData = data
        notifyDataSetChanged()
    }
}