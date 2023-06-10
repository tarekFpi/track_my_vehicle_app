package com.singularity.trackmyvehicle.view.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_distance_table.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Created by Imran Chowdhury on 2020-02-11.
 */

class DistanceReportViewHolder (
        override val containerView: View,
        private val daily : Boolean = true
): RecyclerView.ViewHolder(containerView), LayoutContainer {


    fun bind(model: DistanceReport) {
        mModel = model
        val parsedDateTime = DateTime.parse(model.date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        val start =  if(daily) parsedDateTime.withTimeAtStartOfDay()
        else parsedDateTime.withTime(parsedDateTime.hourOfDay,0,0,0)
        val end = if(daily) start.plusDays(1).minusMinutes(1) else start.plusHours(1).minusMinutes(1)
        containerView.txtFrom.text = start.toString("yyyy-MM-dd HH:mm:ss")
        containerView.txtTo.text = end.toString("yyyy-MM-dd HH:mm:ss")
        containerView.txtDistance.text = if(!daily) {String.format("%.2f KM",(model.km?.toFloatOrNull() ?: 0F)/1000)} else String.format("%.2f KM",(model.km?.toFloatOrNull() ?: 0F))
    }

    private var mModel: DistanceReport? = null



    companion object {
        fun create(parent: ViewGroup,daily : Boolean = true): DistanceReportViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_distance_table, parent, false)
            return DistanceReportViewHolder(itemView,daily)
        }
    }
}