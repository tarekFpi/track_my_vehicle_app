package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import org.greenrobot.eventbus.EventBus
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 3/7/21.
 */
data class VirtualWatchmanActivationModel (
        @SerializedName("Error")
         val error : ErrorResponse? = null,
        @SerializedName("User")
         val user : UserResponse? = null,
        @SerializedName("Response")
         val response : ApiResponse? = null

) : Serializable

data class ErrorResponse(
        @SerializedName("Code")
        val code : Int? = 0,
        @SerializedName("Description")
        val description : String? = ""

) : Serializable

data class UserResponse(
        @SerializedName("ID")
        val iD : Int? = 0,
        @SerializedName("GroupIdentifierHighest")
        val groupIdentifierHighest : String? = "",
        @SerializedName("Name")
        val name : String? = ""

) : Serializable

data class ApiResponse(
        @SerializedName("Status")
        val status : String? = "",
        @SerializedName("Parking")
        val parking : Parking? = null
) : Serializable

data class Parking(
        @SerializedName("ParkingID")
        var parkingID : String? = "",
        @SerializedName("TerminalID")
        var terminalID : String? = "",
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
        @SerializedName("GeoLocationPositionIDLandmark")
        var geoLocationPositionIDLandmark : String? = "",
        @SerializedName("GeoLocationPositionLandmarkDistanceMeter")
        var geoLocationPositionLandmarkDistanceMeter : String? = "",
        @SerializedName("ParkingNotificationTime")
        var parkingNotificationTime : String? = "",
        @SerializedName("ParkingNotificationCount")
        var parkingNotificationCount : String? = "",
        @SerializedName("ParkingIsActive")
        var parkingIsActive : String? = "",
        @SerializedName("UserIDInserted")
        var userIDInserted : String? = "",
        @SerializedName("UserIDUpdated")
        var userIDUpdated : String? = "",
        @SerializedName("UserIDLocked")
        var userIDLocked : String? = "",
        @SerializedName("TimeInserted")
        var timeInserted : String? = "",
        @SerializedName("TimeUpdated")
        var timeUpdated : String? = "",
        @SerializedName("TimeLocked")
        var timeLocked : String? = ""
) : Serializable