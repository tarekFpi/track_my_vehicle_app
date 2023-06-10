package com.singularity.trackmyvehicle.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.view.viewholder.NotificationViewHolder

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */

class NotificationListAdapter(
        private var mCallback: OnItemClickCallback<Notification>
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private var asyncListDiffer: AsyncListDiffer<Notification> = AsyncListDiffer(this,
            DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder.createViewHolder(parent, mCallback)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.setUpListener()
        asyncListDiffer.currentList[position]?.let {
            holder.bind(it)
        }
    }

    fun setItems(mData: MutableList<Notification>) {
        asyncListDiffer.submitList(mData)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }
}