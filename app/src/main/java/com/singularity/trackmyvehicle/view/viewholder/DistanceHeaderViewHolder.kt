package com.singularity.trackmyvehicle.view.viewholder

import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_distance_table.view.*

/**
 * Created by Imran Chowdhury on 2020-02-11.
 */

class DistanceHeaderViewHolder(
        override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind() {
        containerView.txtFrom.text = itemView.context.getString(R.string.from)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            containerView.txtFrom.textAlignment = View.TEXT_ALIGNMENT_CENTER
            containerView.txtTo.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        containerView.txtFrom.setTypeface(containerView.txtFrom.typeface,Typeface.BOLD)
        containerView.txtTo.setTypeface(containerView.txtTo.typeface,Typeface.BOLD)
        containerView.txtDistance.setTypeface(containerView.txtTo.typeface,Typeface.BOLD)
        containerView.txtTo.text = itemView.context.getString(R.string.to)
        containerView.txtDistance.text = itemView.context.getString(R.string.distance)
    }

    companion object {
        fun create(parent: ViewGroup): DistanceHeaderViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_distance_table, parent, false)
            return DistanceHeaderViewHolder(itemView)
        }
    }
}