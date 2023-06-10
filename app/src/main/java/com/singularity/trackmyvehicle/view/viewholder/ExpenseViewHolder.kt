package com.singularity.trackmyvehicle.view.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback

class ExpenseViewHolder(itemView: View,
                        private val mCallback: OnItemClickCallback<Expense>?
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


    private val mDescriptionText: TextView = itemView.findViewById(R.id.description)
    private val mExpenseHeaderText: TextView = itemView.findViewById(R.id.header)
    private val mDateText: TextView = itemView.findViewById(R.id.date)
    private val mAmountText: TextView = itemView.findViewById(R.id.amount)

    init {
        itemView.setOnClickListener(this)
    }

    private var mModel: Expense? = null

    fun bind(model: Expense?) {
        mModel = model
        mDescriptionText.text = mModel?.description
        mExpenseHeaderText.text = mModel?.expenseHeader
        mDateText.text = mModel?.date
        mAmountText.text = "Tk" + mModel?.amount.toString()
    }

    companion object {
        fun viewHolder(parent: ViewGroup, callback: OnItemClickCallback<Expense>?): ExpenseViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_expense, parent, false)

            return ExpenseViewHolder(itemView, callback)
        }
    }

    override fun onClick(view: View?) {
        if (mModel != null) {
            mCallback?.onClick(mModel!!)
        }
    }
}