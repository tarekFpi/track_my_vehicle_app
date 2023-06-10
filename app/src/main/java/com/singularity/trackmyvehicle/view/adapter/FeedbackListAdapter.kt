package com.singularity.trackmyvehicle.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.FeedBackItemViewHolder

class FeedbackListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mData: List<Feedback>? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedBackItemViewHolder) {
            holder.bind(mData?.get(position))
        }
    }

    var callback: OnItemClickCallback<Feedback>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedBackItemViewHolder.viewHolder(parent, callback)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    fun setData(data: List<Feedback>?) {
        mData = data?.sortedByDescending { it.raisedOn }
        notifyDataSetChanged()
    }

}