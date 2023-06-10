package com.singularity.trackmyvehicle.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.singularity.trackmyvehicle.R

/**
 * Created by Kariba Yasmin on 8/19/21.
 */
class CustomInfoDialogVehicleRouteAnalytics {

    fun showDialog(activity: Activity?) {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog_custom_info_vehicle_route_analytics)

        val dialogButton: AppCompatImageView = dialog?.findViewById(R.id.imageView_cross) as AppCompatImageView

        dialogButton.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }
}