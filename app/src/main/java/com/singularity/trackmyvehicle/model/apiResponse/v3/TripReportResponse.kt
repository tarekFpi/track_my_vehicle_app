package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.utils.customizeDateTimeFormat
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 11/22/21.
 */
class TripReportResponse (
        @SerializedName("Error")
        var reportErrorResponse : ReportErrorResponse? = null,
        @SerializedName("Response")
        var reportSuccessResponse : ReportSuccessResponse? = null

) : Serializable {

    class ReportErrorResponse (
            @SerializedName("Code")
            var code : Int? = 0,
            @SerializedName("Description")
            var description : String? = ""

    ) : Serializable

    class ReportSuccessResponse (
            @SerializedName("Data")
            var reportDataList : List<ReportDataList>? = ArrayList()

    ) : Serializable {
        class ReportDataList (
             @SerializedName("ProviderCode")
             var  providerCode : String? = "",
             @SerializedName("CustomerName")
             var  customerName : String? = "",
             @SerializedName("TerminalAssignmentCode")
             var terminalAssignmentCode : String? = "",
             @SerializedName("CarrierRegistrationNumber")
             var carrierRegistrationNumber : String? = "",
             @SerializedName("CarrierName")
             var carrierName : String? = "",
             @SerializedName("TripTimeBegin")
             var tripTimeBegin : String? = "",
             @SerializedName("TripTimeEnd")
             var tripTimeEnd : String? = "",
             @SerializedName("TripDuration")
             var tripDuration : String? = "",
             @SerializedName("TripDurationTime")
             var tripDurationTime : String? = "",
             @SerializedName("TripDistanceKm")
             var tripDistanceKm : String? = "",
             @SerializedName("TripVelocityKmH")
             var tripVelocityKmH : String? = "",
             @SerializedName("GeoLocationNameBegin")
             var geoLocationNameBegin : String? = "",
             @SerializedName("GeoLocationPositionLandmarkDistanceKmBegin")
             var geoLocationPositionLandmarkDistanceKmBegin : String? = "",
             @SerializedName("GeoLocationNameEnd")
             var geoLocationNameEnd : String? = "",
             @SerializedName("GeoLocationPositionLandmarkDistanceKmEnd")
             var geoLocationPositionLandmarkDistanceKmEnd : String? = ""

        ) : Serializable {

            fun getTripTimeBeginCustomizeDate() : String? {

                return tripTimeBegin?.let { customizeDateTimeFormat(it) }
            }

            fun getTripTimeEndCustomizeDate() : String? {

                return tripTimeEnd?.let { customizeDateTimeFormat(it) }
            }

            fun getBstId(): String {
                return if (terminalAssignmentCode == "") "" else "BST $terminalAssignmentCode"
            }
        }
    }
}