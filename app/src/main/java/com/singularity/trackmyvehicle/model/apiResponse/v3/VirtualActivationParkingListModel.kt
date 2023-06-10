package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 3/11/21.
 */
data class VirtualActivationParkingListModel (
        @SerializedName("Error")
        var error : ErrorApiResponse? = null,
        @SerializedName("User")
        var user : UserApiResponse? = null,
        @SerializedName("Response")
        var response : SuccessApiResponse? = null

): Serializable

data class ErrorApiResponse(
        @SerializedName("Code")
        var code : Int? = 0,
        @SerializedName("Description")
        var description : String? = ""
): Serializable

data class UserApiResponse(
        @SerializedName("ID")
        var iD : Int? = 0,
        @SerializedName("GroupIdentifierHighest")
        var groupIdentifierHighest : String? = "",
        @SerializedName("Name")
        var name : String? = ""
) : Serializable

data class SuccessApiResponse(
        @SerializedName("Parking")
        var parking : List<ParkingResponse>? = ArrayList(),
        @SerializedName("Server")
        var server : ServerResponse? = null

) : Serializable

data class ParkingResponse(
        @SerializedName("ParkingID")
        var parkingID : String? = "",
        @SerializedName("ParkingTime")
        var parkingTime : String? = "",
        @SerializedName("ParkingTimeExpiry")
        var parkingTimeExpiry : String? = "",
        @SerializedName("ParkingLatitude")
        var parkingLatitude : String? = "",
        @SerializedName("ParkingLongitude")
        var parkingLongitude : String? = "",
        @SerializedName("ParkingRadiusMeter")
        var parkingRadiusMeter : String? = "",
        @SerializedName("TerminalID")
        var terminalID : String? = "",
        @SerializedName("TerminalAssignmentCode")
        var terminalAssignmentCode : String? = "",
        @SerializedName("CarrierID")
        var carrierID : String? = "",
        @SerializedName("CarrierRegistrationNumber")
        var carrierRegistrationNumber : String? = "",
        @SerializedName("CarrierName")
        var carrierName : String? = ""

) : Serializable

data class ServerResponse(
        @SerializedName("Time")
        var time : String? = ""
) : Serializable
