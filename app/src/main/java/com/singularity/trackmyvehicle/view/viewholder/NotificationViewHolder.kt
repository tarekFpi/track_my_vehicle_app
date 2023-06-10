package com.singularity.trackmyvehicle.view.viewholder

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_notification.view.*

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */

class NotificationViewHolder(
        override val containerView: View,
        private var mCallback: OnItemClickCallback<Notification>? = null
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    private var mModel: Notification? = null

    fun bind(model: Notification) {
        mModel = model
        itemView.findViewById<TextView>(R.id.tvNotificationTitle).text = model.subject
        itemView.findViewById<TextView>(R.id.tvNotificationDate).text = model.time?.toString(
                "hh:mm a dd, MMM YYYY")

        itemView.findViewById<TextView>(R.id.tvNotificationDetails)
                .text = if (!model.messageContentPlain.isNullOrBlank()) model.messageContentPlain else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(model.message, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(model.message)
        }
        itemView.findViewById<View>(R.id.containerCause).visibility = View.GONE
    }

    fun setUpListener() {
        containerView.containerCause.setOnClickListener {
            if (containerView.containerCause.isExpanded) {
                containerView.containerCause.collapse()
            } else {
                containerView.containerCause.expand()
            }
        }
    }

    companion object {
        fun createViewHolder(
                parent: ViewGroup,
                mCallback: OnItemClickCallback<Notification>? = null
        ): NotificationViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(itemView, mCallback)
        }
    }

}