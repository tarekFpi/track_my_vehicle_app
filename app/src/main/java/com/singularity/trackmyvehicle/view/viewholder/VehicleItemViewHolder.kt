package com.singularity.trackmyvehicle.view.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.utils.getRelativeTimeFromNow
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_vehicle_list_v3.view.*
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/10/18.
 */
class VehicleItemViewHolder(override val containerView: View,
                            private val mCallback: OnItemClickCallback<Terminal>?
) : RecyclerView.ViewHolder(containerView), View.OnClickListener, LayoutContainer {


    @Inject
    lateinit var userSource: UserSource
    private val mTextName: TextView = itemView.findViewById(R.id.name)
    private val mTextVRN: TextView = itemView.findViewById(R.id.vrn)

    init {
        itemView.setOnClickListener(this)
        itemView.findViewById<View>(R.id.containerVehicleItem).setOnClickListener(this)
        VehicleTrackApplication.appComponent?.inject(this)
    }

    private var mModel: Terminal? = null

    fun bind(model: Terminal?) {
        mModel = model
        mTextName.text = mModel?.bstid
        mTextVRN.text = mModel?.vrn
        containerView.ilvLocation.value?.text = mModel?.geoLocationName
        containerView.ilvSpeed.value?.text = StringBuilder().apply {
            this.append(String.format("%.1f", mModel?.terminalDataVelocityLast?.toFloatOrNull()))
            this.append(" km/h")
        }.toString()
        val travelled = mModel?.travelled
        containerView.ilvTraveled.value?.text = when (travelled) {
            null -> "-- KM"
            else -> String.format("%.1f KM", travelled / 1000)
        }
        containerView.ilvUpdatedAt.value?.text = model?.terminalDataTimeLast?.let {
            getRelativeTimeFromNow(it)
        }
                ?: "--:--"
        containerView.imgEngineStatus.setImageDrawable(
                if (model?.terminalDataTimeLast?.isBefore(DateTime.now().minusDays(
                                1)) == true || model?.terminalDataIsAccOnLast?.contains(
                                "--") == true) {
                    ContextCompat.getDrawable(itemView.context, R.drawable.engine_disable)
                } else if (model?.terminalDataIsAccOnLast == "1" && (model.terminalDataVelocityLast?.toFloatOrNull()
                                ?: 0.0) == 0.0) ContextCompat.getDrawable(itemView.context,
                        R.drawable.engine_idle)
                else if (model?.terminalDataIsAccOnLast == "1") ContextCompat.getDrawable(
                        itemView.context, R.drawable.engine_on)
                else ContextCompat.getDrawable(itemView.context, R.drawable.engine_off)
        )
        containerView.containerStatusDetails.visibility =
                if (userSource == UserSource.VERSION_2) View.GONE
                else if (model?.isSuspended() == true) View.GONE
                else View.VISIBLE
        containerView.imgEngineStatus.visibility = when {
            userSource == UserSource.VERSION_2 -> View.GONE
            model?.isSuspended() == true -> View.GONE
            else -> View.VISIBLE
        }
        containerView.containerSuspended.visibility =
                when {
                    model?.isSuspended() == true -> View.VISIBLE
                    else -> View.GONE
                }
    }

    companion object {
        fun viewHolder(parent: ViewGroup,
                       callback: OnItemClickCallback<Terminal>?): VehicleItemViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_vehicle_list_v3, parent, false)
            return VehicleItemViewHolder(itemView, callback)
        }
    }

    override fun onClick(p0: View?) {
        if (mModel != null) {
            mCallback?.onClick(mModel!!)
        }
    }
}