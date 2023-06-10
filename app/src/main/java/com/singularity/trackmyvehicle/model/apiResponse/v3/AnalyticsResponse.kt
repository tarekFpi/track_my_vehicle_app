package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 4/12/21.
 */
class AnalyticsResponse (
        @SerializedName("Error")
        var analyticsErrorResponse : AnalyticsErrorResponse? = null,
        @SerializedName("User")
        var analyticsUserResponse : AnalyticsUserResponse? = null,
        @SerializedName("Response")
        var analyticsSuccessResponse : AnalyticsSuccessResponse? = null

) : Serializable {

    class AnalyticsErrorResponse (
            @SerializedName("Code")
            var analyticsErrorCode : Int? = 0,
            @SerializedName("Description")
            var description : String? = ""
    ) :Serializable

    class AnalyticsUserResponse (
            @SerializedName("ID")
            var analyticsUserID : Int? = 0,
            @SerializedName("GroupIdentifierHighest")
            var analyticsUserGroupIdentifierHighest :  String? = "",
            @SerializedName("Name")
            var analyticsUserName : String? = ""

    ) : Serializable

    class AnalyticsSuccessResponse (
            @SerializedName("Parameter")
            var parameter : AnalyticsParameterResponse? = null,
            @SerializedName("Process")
            var process : AnalyticsProcessResponse? = null,
            @SerializedName("Status")
            var analyticsStatus : List<AnalyticsStatus>? = null,
            @SerializedName("Daily")
            var analyticsDaily : List<AnalyticsDaily>? = null,
            @SerializedName("Event")
            var analyticsEvent : List<AnalyticsEvent>? = null,
            @SerializedName("Terminal")
            var analyticsTerminal : List<AnalyticsTerminal>? = null,
            @SerializedName("DriveSafety")
            var analyticsDriveSafetyRank : AnalyticsDriveSafetyRank? = null


    ) : Serializable {

        class AnalyticsParameterResponse (
                @SerializedName("UserID")
                var userID : String? = "",
                @SerializedName("DaysFrom")
                var daysFrom : String? = "",
                @SerializedName("Days")
                var days : String? = "",
                @SerializedName("UserGroupIdentifierHighest")
                var userGroupIdentifierHighest : String? = "",
                @SerializedName("CustomerID")
                var customerID : String? = "",
                @SerializedName("CustomerName")
                var customerName : String? = "",
                @SerializedName("TimeFrom")
                var timeFrom : String? = "",
                @SerializedName("TimeTo")
                var timeTo : String? = "",
                @SerializedName("CacheHour")
                var cacheHour : Int? = 0
        ) : Serializable

    }

    class AnalyticsProcessResponse (
            @SerializedName("Status")
            var processStatus : String? = "",
            @SerializedName("Duration")
            var processDuration : String? = ""
    ) : Serializable

    class AnalyticsStatus (
            @SerializedName("TerminalCount")
            var terminalCount : String? = "",
            @SerializedName("TerminalState")
            var terminalState : String? = "",
            @SerializedName("TerminalStateColor")
            var terminalStateColor : String? = ""
    ) : Serializable

    class AnalyticsDaily (
            @SerializedName("TerminalDataHourlyTimeTo")
            var terminalDataHourlyTimeTo : String? = "",
            @SerializedName("TerminalDistanceKilometer")
            var terminalDistanceKilometer : String? = "",
            @SerializedName("TerminalCount")
            var terminalCount : String? = ""
    ) : Serializable

    class AnalyticsEvent (
            @SerializedName("EventDate")
            var eventDate : String? = "",
            @SerializedName("TripCount")
            var tripCount : String? = "",
            @SerializedName("GeofenceExitCount")
            var geofenceExitCount : String? = "",
            @SerializedName("OverspeedCount")
            var overspeedCount : String? = ""
    ): Serializable

    class AnalyticsTerminal (
            @SerializedName("TerminalID")
            var terminalID : String? = "",
            @SerializedName("TerminalAssignmentCode")
            var terminalAssignmentCode :String? = "",
            @SerializedName("TerminalAssignmentIsSuspended")
            var terminalAssignmentIsSuspended : String? = "",
            @SerializedName("TerminalDataTimeLast")
            var terminalDataTimeLast : String? = "",
            @SerializedName("TerminalState")
            var terminalState : String? = "",
            @SerializedName("CarrierRegistrationNumber")
            var carrierRegistrationNumber : String? = "",
            @SerializedName("CarrierName")
            var carrierName : String? = "",
            @SerializedName("CarrierLookupCaption")
            var carrierLookupCaption : String? = ""

    ): Serializable

    class AnalyticsDriveSafetyRank (
            @SerializedName("Label")
            var label : List<Label>? = null,
            @SerializedName("Rank")
            var rank : List<Rank>? = null

    ): Serializable {
        class Label (
                @SerializedName("Label")
                var label : String? = "",
                @SerializedName("Color")
                var color : String? = ""
        ) : Serializable

        class Rank (
                @SerializedName("TerminalID")
                var terminalID : String? = "",
                @SerializedName("TerminalEventCountOverspeed")
                var terminalEventCountOverSpeed : String? = "",
                @SerializedName("TerminalEventCountAccelerationBrake")
                var terminalEventCountAccelerationBrake : String? = "",
                @SerializedName("TerminalEventCountAccelerationSudden")
                var terminalEventCountAccelerationSudden : String? = "",
                @SerializedName("DriveSafetyLabel")
                var driveSafetyLabel : String? = "",
                @SerializedName("DriveSafetyOrder")
                var driveSafetyOrder : String? = "",
                @SerializedName("DriveSafetyColor")
                var driveSafetyColor : String? = "",
                @SerializedName("TerminalEventPerDistance")
                var terminalEventPerDistance : String? = ""
        ) : Serializable{
            fun modifiedTotalAlerts() : Int {

                var totalAlerts = (terminalEventCountOverSpeed?.toInt() ?: 0) + (terminalEventCountAccelerationBrake?.toInt() ?: 0) + (terminalEventCountAccelerationSudden?.toInt() ?: 0)

                return totalAlerts ?: 0
            }
        }
    }

}


