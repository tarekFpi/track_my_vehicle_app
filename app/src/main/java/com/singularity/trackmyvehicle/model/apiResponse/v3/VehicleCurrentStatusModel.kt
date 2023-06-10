package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 10/6/21.
 */
class VehicleCurrentStatusModel (
        @SerializedName("id")
        var id : Int = 0,
        @SerializedName("vehicleCount")
        var vehicleCount : Int = 0,
        @SerializedName("title")
        var title : String = ""

) : Serializable