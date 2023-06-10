package com.singularity.trackmyvehicle.model.apiResponse.v3
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by Imran Chowdhury on 2020-02-03.
 */


data class TerminalDataMinutelyResponseWrapper(
        @Expose
        @SerializedName("TerminalDataMinutely")
        var data: List<TerminalDataMinutely>? = null
)

@Entity(tableName = "terminal_data_minutely")
data class TerminalDataMinutely(
    @PrimaryKey
    @SerializedName("TerminalDataMinutelyID")
    var terminalDataMinutelyID: Int = 0,
    @SerializedName("TerminalDataMinutelyTimeFirst")
    var terminalDataMinutelyTimeFirst: String? = null,
    @SerializedName("TerminalDataMinutelyTimeLast")
    var terminalDataMinutelyTimeLast: String? = null,
    @SerializedName("TerminalDataMinutelyCount")
    var terminalDataMinutelyCount: String? = null,
    @SerializedName("TerminalID")
    var terminalID: String? = null,
    @SerializedName("TerminalDataMinutelyCoordinateList")
    var terminalDataMinutelyCoordinateList: String? = null,
    @SerializedName("TerminalDataMinutelyIsAccOn")
    var terminalDataMinutelyIsAccOn: String? = null,
    @SerializedName("TerminalDataMinutelyIsDoorOpen")
    var terminalDataMinutelyIsDoorOpen: String? = null,
    @SerializedName("TimeInserted")
    var timeInserted: String? = null,
    @SerializedName("ProviderCode")
    var providerCode: String? = null,
    @SerializedName("ProviderName")
    var providerName: String? = null,
    @SerializedName("TerminalAssignmentCode")
    var TerminalAssignmentIsSuspended: String? = null,
    @SerializedName("TerminalAssignmentIsSuspended")
    var terminalAssignmentCode: String? = null,
    @SerializedName("CustomerCode")
    var customerCode: String? = null,
    @SerializedName("CustomerName")
    var customerName: String? = null,
    @SerializedName("CarrierRegistrationNumber")
    var carrierRegistrationNumber: String? = null,
    @SerializedName("CarrierName")
    var carrierName: String? = null,
    @SerializedName("CarrierTypeName")
    var carrierTypeName: String? = null,
    @SerializedName("TerminalDataMinutelyVelocityKmH")
    var terminalDataMinutelyVelocityKmH: String? = null,
    @SerializedName("TerminalDataMinutelyLatitude")
    var terminalDataMinutelyLatitude: String? = null,
    @SerializedName("TerminalDataMinutelyLongitude")
    var terminalDataMinutelyLongitude: String? = null,
    @SerializedName("GeoLocationName")
    var geoLocationName: String? = null,
    @SerializedName("GeoLocationPositionLandmarkDistanceMeter")
    var geoLocationPositionLandmarkDistanceMeter: String? = null
)