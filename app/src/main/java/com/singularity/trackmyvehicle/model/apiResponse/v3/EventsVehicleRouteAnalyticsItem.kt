package com.singularity.trackmyvehicle.model.apiResponse.v3

import android.content.Context
import android.graphics.drawable.Drawable
import com.google.android.libraries.places.internal.dr
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.Constants
import kotlinx.android.synthetic.main.view_vehicle_route_analytics.view.*
import java.io.Serializable
import com.singularity.trackmyvehicle.R

/**
 * Created by Kariba Yasmin on 8/22/21.
 */
class EventsVehicleRouteAnalyticsItem (

    @SerializedName("itemNumber")
    var itemNumber : Int? = 0,
    @SerializedName("itemName")
    var itemName : String? = "",
    @SerializedName("checked")
    var checked : Boolean? = false

    ) : Serializable {

        fun modifiedItemImage(context: Context, imageId: Int) : Drawable? {
            if (imageId == 1) {
                return context.getDrawable(R.drawable.ic_parking)

            } else if (imageId == 2) {
                return context.getDrawable(R.drawable.ic_accelaration)

            } else if (imageId == 3) {
                return context.getDrawable(R.drawable.ic_harsh_break)

            } else if (imageId == 4) {
                return context.getDrawable(R.drawable.ic_speed_violation)

            } else if (imageId == 5) {
                return context.getDrawable(R.drawable.ic_engine_start)

            } else if (imageId == 6) {
                return context.getDrawable(R.drawable.ic_idle)

            } else if (imageId == 7) {
                return context.getDrawable(R.drawable.ic_power_down)
            }

            return null
        }

        fun modifiedItemName() : String {
            return itemName ?: ""
        }
    }