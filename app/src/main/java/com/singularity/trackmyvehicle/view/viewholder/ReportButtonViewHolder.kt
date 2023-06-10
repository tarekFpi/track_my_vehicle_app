package com.singularity.trackmyvehicle.view.viewholder

import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_report_button.view.*

class ReportButtonViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(text: String, @DrawableRes icon: Int) {
        containerView.btnReport.text = text
        containerView.btnReport.icon = ContextCompat.getDrawable(itemView.context,icon)
        val lp = containerView.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            val flexboxLp = lp as FlexboxLayoutManager.LayoutParams
            flexboxLp.flexGrow = 1.0f
        }
    }
}
