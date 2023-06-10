package com.singularity.trackmyvehicle.view.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import org.joda.time.DateTime
import org.joda.time.DateTimeField
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class SpeedReportViewHolder(itemView: View,
                            private val mCallback: OnItemClickCallback<SpeedAlertReport>? = null
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


    private val mDescriptionText: TextView = itemView.findViewById(R.id.description)
    private val mExpenseHeaderText: TextView = itemView.findViewById(R.id.header)
    private val mDateText: TextView = itemView.findViewById(R.id.date)
    private val mAmountText: TextView = itemView.findViewById(R.id.amount)

    init {
        itemView.setOnClickListener(this)
    }

    private var mModel: SpeedAlertReport? = null

    fun bind(model: SpeedAlertReport?) {
        mModel = model
        mDescriptionText.text = if(mModel?.place == "false") "Speed limit violated by vehicle ${mModel?.bstId}" else mModel?.place ?: "Speed violated at ${DateTime.parse(mModel?.date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                .toString("hh:mm a dd-MMM-yy")}"
        mExpenseHeaderText.text = "${mModel?.speed} Km/hr"
        mDateText.text = DateTime.parse(mModel?.date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                .toString("hh:mm a dd-MMM-yy")


//        mAmountText.text = "Tk" + mModel?.amount.toString()
    }

    companion object {
        fun viewHolder(parent: ViewGroup, callback: OnItemClickCallback<SpeedAlertReport>? = null): SpeedReportViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_speed_violations, parent, false)

            return SpeedReportViewHolder(itemView, callback)
        }
    }

    override fun onClick(view: View?) {
        if (mModel != null) {
            mCallback?.onClick(mModel !!)
        }
    }
}