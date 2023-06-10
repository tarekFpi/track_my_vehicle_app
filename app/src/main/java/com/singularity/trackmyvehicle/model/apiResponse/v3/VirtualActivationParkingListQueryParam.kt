package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 3/11/21.
 */
class VirtualActivationParkingListQueryParam : Serializable{
    @SerializedName("_Script")
    var script : String = ""
    @SerializedName("TerminalID")
    var terminalID : Int = 0
    @SerializedName("ParkingIsActive")
    var ParkingIsActive : Int = 0
}


