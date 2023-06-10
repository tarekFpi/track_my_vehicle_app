package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 3/7/21.
 */
 class VirtualWatchmanActivationPostBody : Serializable{
    @SerializedName("_Script")
    var script : String = ""
    @SerializedName("TerminalID")
    var terminalID : Int = 0
    @SerializedName("ParkingDurationMinute")
    var parkingDurationMinute : Int = 0
    @SerializedName("ParkingRadiusMeter")
    var parkingRadiusMeter : Int = 0
}