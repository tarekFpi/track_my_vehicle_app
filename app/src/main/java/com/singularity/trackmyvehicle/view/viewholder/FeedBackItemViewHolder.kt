package com.singularity.trackmyvehicle.view.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class FeedBackItemViewHolder(itemView: View,
                             private val mCallback: OnItemClickCallback<Feedback>?
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val mTextFeedbackId: TextView = itemView.findViewById(R.id.txtFeedbackId)
    val mTextFeedback: TextView = itemView.findViewById(R.id.txtFeedback)
    val mTextRaisedOn: TextView = itemView.findViewById(R.id.txtRaisedOn)
    val mTextSolvedOn: TextView = itemView.findViewById(R.id.txtSolvedOn)
    val mTxtVrn: TextView = itemView.findViewById(R.id.txtVrn)
    val mTxtStatus: TextView = itemView.findViewById(R.id.txtStatus)
    val mReplyButton: Button = itemView.findViewById(R.id.btnReply)

    @Inject
    lateinit var userSource: UserSource

    init {
        VehicleTrackApplication.appComponent?.inject(this)
//        itemView.setOnClickListener(this)
        mReplyButton.setOnClickListener(this)
        if(userSource.identifier == UserSource.VERSION_3.identifier) {
            itemView.findViewById<View>(R.id.containerSolvedOn).visibility = View.GONE
        }
    }

    private var mModel: Feedback? = null

    fun bind(model: Feedback?) {
        mModel = model
        mTextFeedback?.text = mModel?.feedback
        mTextFeedbackId?.text = mModel?.feedbackId
        try {
            mTextRaisedOn?.text = DateTime.parse(mModel?.raisedOn, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toString("dd-MMM-yy hh:mm a")
        }catch (ex: Exception) {
            mTextRaisedOn?.text = "N/A"
        }

        try {
            mTextSolvedOn?.text = DateTime.parse(mModel?.solvedOn, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toString("dd-MMM-yy hh:mm a")
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            mTextSolvedOn?.text = "N/A"
        }
        mTxtStatus.text = model?.feedbackStatus?.toUpperCase() ?: "N/A"
        mTxtVrn.text = model?.vrn ?: "VRN Not Available"
    }

    companion object {
        fun viewHolder(parent: ViewGroup, callback: OnItemClickCallback<Feedback>?): FeedBackItemViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feedback, parent, false)

            return FeedBackItemViewHolder(itemView, callback)
        }
    }

    override fun onClick(view: View?) {
        if (mModel != null) {
            mCallback?.onClick(mModel !!)
        }
    }
}