package com.singularity.trackmyvehicle.view.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RemarkItemViewHolder(itemView: View,
                           private val mCallback: OnItemClickCallback<FeedbackRemark>?
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val mTextFeedbackId: TextView = itemView.findViewById(R.id.txtFeedbackId)
    val mTextFeedback: TextView = itemView.findViewById(R.id.txtFeedback)
    val mTextRaisedOn: TextView = itemView.findViewById(R.id.txtRaisedOn)
    val mTextSolvedOn: TextView = itemView.findViewById(R.id.txtSolvedOn)

    init {

        itemView.setOnClickListener(this)
    }

    private var mModel: FeedbackRemark? = null

    fun bind(model: FeedbackRemark?) {
        mModel = model
        mTextFeedback?.text = mModel?.remarks?.toUpperCase()
        mTextFeedbackId?.text = mModel?.feedbackId
        mTextRaisedOn?.text = DateTime.parse(mModel?.updateOn, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toString("dd-MMM-yy hh:mm a")
        mTextSolvedOn?.text = mModel?.updateBy?.toUpperCase()
    }

    companion object {
        fun viewHolder(parent: ViewGroup, callback: OnItemClickCallback<FeedbackRemark>?): RemarkItemViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_remark, parent, false)

            return RemarkItemViewHolder(itemView, callback)
        }
    }

    override fun onClick(view: View?) {
        if (mModel != null) {
            mCallback?.onClick(mModel !!)
        }
    }
}