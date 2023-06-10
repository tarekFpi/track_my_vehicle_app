package com.singularity.trackmyvehicle.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.ExpenseViewHolder

class ExpenseListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mData: List<Expense>? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ExpenseViewHolder) {
            holder.bind(mData?.get(position))
        }
    }

    var callback: OnItemClickCallback<Expense>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ExpenseViewHolder.viewHolder(parent, callback)
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    fun setData(data: List<Expense>?) {
        mData = data
        notifyDataSetChanged()
    }

}