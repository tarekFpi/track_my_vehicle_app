package com.singularity.trackmyvehicle.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.VehicleItemViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Sadman Sarar on 3/10/18.
 */

class VehicleListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mData: List<Terminal>? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VehicleItemViewHolder) {
            holder.bind(mData?.get(position))
        }
    }

    var callback: OnItemClickCallback<Terminal>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VehicleItemViewHolder.viewHolder(parent, callback)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    fun setData(data: List<Terminal>) {



        val newItem = data.toList()
        val oldItem = mData?.toList() ?: listOf()
        val callback = object : DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItem[oldItemPosition].bid == newItem[newItemPosition].bid
            }

            override fun getOldListSize(): Int {
                return oldItem.size
            }

            override fun getNewListSize(): Int {
                return newItem.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItem[oldItemPosition].equals(newItem[newItemPosition].bid)
            }
        }
        mData = data
        DiffUtil.calculateDiff(callback).dispatchUpdatesTo(this)
    }
}