package com.singularity.trackmyvehicle.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.view.viewholder.DistanceHeaderViewHolder
import com.singularity.trackmyvehicle.view.viewholder.DistanceReportViewHolder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Imran Chowdhury on 2020-02-11.
 */

class DistanceAdapter(
        private val daily : Boolean = true
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mData: MutableList<DistanceReport>? = null

    private val VIEW_TYPE_HEADER = 3333
    private val VIEW_TYPE_REPORT = 4444

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            return DistanceHeaderViewHolder.create(parent)
        }
        return DistanceReportViewHolder.create(parent,daily)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_HEADER
        }
        return VIEW_TYPE_REPORT
    }

    override fun getItemCount(): Int {
        return (mData?.size ?: 0) + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0 && holder is DistanceHeaderViewHolder) {
            holder.bind()
        } else if(position != 0 && holder is DistanceReportViewHolder){
            mData?.get(position - 1)?.let { holder.bind(it) }
        }
    }

    fun setData(data: MutableList<DistanceReport>?) {
        mData = data?.filter { DateTime.parse(it.date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).isBeforeNow }?.toMutableList()
        notifyDataSetChanged()
    }

}