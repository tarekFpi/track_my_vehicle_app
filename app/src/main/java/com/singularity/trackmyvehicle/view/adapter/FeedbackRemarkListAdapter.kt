package com.singularity.trackmyvehicle.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.RemarkItemViewHolder

class FeedbackRemarkListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mData: List<FeedbackRemark>? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RemarkItemViewHolder) {
            holder.bind(mData?.get(position))
        }
    }

    var callback: OnItemClickCallback<FeedbackRemark>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RemarkItemViewHolder.viewHolder(parent, callback)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    fun setData(data: List<FeedbackRemark>?) {
        mData = data
        notifyDataSetChanged()
    }

}