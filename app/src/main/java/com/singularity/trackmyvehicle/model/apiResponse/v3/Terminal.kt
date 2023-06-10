package com.singularity.trackmyvehicle.model.apiResponse.v3

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.BuildConfig
import org.joda.time.DateTime

@Entity(tableName = "terminals")
data class Terminal(
        @Expose
        @PrimaryKey
        @SerializedName("TerminalID")
        var terminalID: Int = 0,
        @Expose
        @SerializedName("TerminalDataIsAccOnLast")
        var terminalDataIsAccOnLast: String? = null,
        @Expose
        @SerializedName("TerminalDataVelocityLast")
        var terminalDataVelocityLast: String? = null,
        @Expose
        @SerializedName("TerminalDataTimeLast")
        var terminalDataTimeLast: DateTime? = null,
        @Expose
        @SerializedName("TerminalDataLatitudeLast")
        var terminalDataLatitudeLast: String? = null,
        @Expose
        @SerializedName("TerminalDataLongitudeLast")
        var terminalDataLongitudeLast: String? = null,
        @Expose
        @SerializedName("GeoLocationPositionLandmarkDistanceMeter")
        var geoLocationPositionLandmarkDistanceMeter: String? = null,
        @Expose
        @SerializedName("TerminalAssignmentCode")
        var terminalAssignmentCode: String? = null,
        @Expose
        @SerializedName("CarrierID")
        var carrierID: String? = null,
        @Expose
        @SerializedName("CarrierRegistrationNumber")
        var carrierRegistrationNumber: String? = null,
        @Expose
        @SerializedName("CarrierName")
        var carrierName: String? = null,
        @Expose
        @SerializedName("CarrierBrand")
        var carrierBrand: String? = null,
        @Expose
        @SerializedName("CarrierModel")
        var carrierModel: String? = null,
        @Expose
        @SerializedName("CarrierColor")
        var carrierColor: String? = null,
        @Expose
        @SerializedName("CarrierTypeName")
        var carrierTypeName: String? = null,
        @Expose
        @SerializedName("CustomerCode")
        var customerCode: String? = null,
        @Expose
        @SerializedName("CustomerName")
        var customerName: String? = null,
        @Expose
        @SerializedName("OperatorName")
        var operatorName: String? = null,
        @Expose
        @SerializedName("OperatorPhone")
        var operatorPhone: String? = null,
        @Expose
        @SerializedName("GeoLocationName")
        var geoLocationName: String? = null,
        @Expose
        @SerializedName("TerminalAssignmentIsSuspended")
        var terminalAssignmentIsSuspended: String? = null,
        @Ignore
        var travelled : Float? = null
) {

    companion object {
        const val PREFIX = BuildConfig.ASSIGNMENT_CODE_PREFIX
    }

    fun isSuspended() : Boolean {
        return terminalAssignmentIsSuspended == "1"
    }

    var bstId: String
        get() = "$PREFIX$terminalAssignmentCode"
        set(value) {}
    var bid: String
        get() = terminalID.toString() ?: ""
        set(value) {}
    var bstid: String
        get() = bstId
        set(value) {}
    var vrn: String
        get() = carrierRegistrationNumber ?: ""
        set(value) {}

    @Ignore
    var sim: String =""
    @Ignore
    var expiryDate: String = ""
    @Ignore
    var dueAmount: String =""

    override fun toString(): String {
        return "$vrn($bstId)"
    }
}