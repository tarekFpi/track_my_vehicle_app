package com.singularity.trackmyvehicle.model.apiResponse.v3

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.utils.TimestampConverter.Companion.df
import org.joda.time.DateTime
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kariba Yasmin on 8/5/21.
 */
class VehicleRouteTerminalData(
        @SerializedName("Error")
        var vehicleRouteErrorResponse: VehicleRouteErrorResponse? = null,
        @SerializedName("Response")
        var vehiclesRouteSuccessResponse: VehicleRouteSuccessResponse? = null

) : Serializable {

    class VehicleRouteErrorResponse(
            @SerializedName("Code")
            var code: Int? = 0,
            @SerializedName("Description")
            var description: String? = ""

    ) : Serializable

    class VehicleRouteSuccessResponse(
            @SerializedName("MotionState")
            var vehicleRouteMotionState: List<VehicleRouteMotionState>? = ArrayList(),
            @SerializedName("Data")
            var vehicleRouteData: List<VehicleRouteData>? = ArrayList(),
            @SerializedName("GeoLocation")
            var vehicleRouteGeoLocationPosition: List<VehicleRouteGeoLocationPosition>? = ArrayList(),
            @SerializedName("Event")
            var vehicleRouteEvent: List<VehicleRouteEvent>? = ArrayList()
            /*@SerializedName("Status")
            var vehicleRouteStatus : List<VehicleRouteStatus>? = ArrayList()*/

    ) : Serializable {

        class VehicleRouteData(
                @SerializedName("TerminalID")
                var terminalID: String? = "",
                @SerializedName("ID")
                var terminalDataID: String? = "",
                @SerializedName("Time")
                var terminalDataTime: String? = "",
                @SerializedName("Latitude")
                var terminalDataLatitude: String? = "",
                @SerializedName("Longitude")
                var terminalDataLongitude: String? = "",
                @SerializedName("IsAccOn")
                var terminalDataIsAccOn: String? = "",
                @SerializedName("VelocityKmH")
                var terminalDataVelocity: String? = "",
                @SerializedName("GeoLocationID")
                var geoLocationPositionIDLandmark: String? = "",
                @SerializedName("GeoLocationDistanceMeter")
                var geoLocationPositionLandmarkDistanceMeter: String? = "",
                @SerializedName("DurationSecond")
                var durationSecond: String? = "",
                @SerializedName("DistanceMeter")
                var distanceMeter: String? = "",
                @SerializedName("VelocityGPSKmH")
                var velocityGPSKmH: String? = "",
                @SerializedName("Bearing")
                var bearing: String? = "",
                @SerializedName("IsPitStopEnd")
                var isPitStopEnd: String? = "",
                @SerializedName("MotionStateID")
                var motionStateID: String? = ""

        ) : Serializable {

            fun convertTimeToDateFormat(): Date{

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateDate = formatter.parse(this.terminalDataTime ?: "1970-01-01 12:00:00")

                return dateDate ?: Date()
            }

        }

        class VehicleRouteGeoLocationPosition(
                @SerializedName("ID")
                var iD: String? = "",
                @SerializedName("Name")
                var name: String? = "",
                @SerializedName("Latitude")
                var latitude: String? = "",
                @SerializedName("Longitude")
                var longitude: String? = ""

        ) : Serializable

        class VehicleRouteMotionState(
                @SerializedName("ID")
                var iD: String? = "",
                @SerializedName("MaximumKmH")
                var maximumKmH: String? = "",
                @SerializedName("Caption")
                var caption: String? = "",
                @SerializedName("Color")
                var color: String? = ""

        ) : Serializable{
             fun modifiedColor() : Int {
                return Color.parseColor(color ?: "#000000")
            }
        }

        class VehicleRouteEvent(
                @SerializedName("TerminalID")
                var terminalID: String? = "",
                @SerializedName("Time")
                var time: String? = "",
                @SerializedName("TerminalEventID")
                var terminalEventID: String? = "",
                @SerializedName("TerminalGeoFenceAreaEventID")
                var terminalGeoFenceAreaEventID: String? = "",
                @SerializedName("Latitude")
                var latitude: String? = "",
                @SerializedName("Longitude")
                var longitude: String? = "",
                @SerializedName("SubjectIdentifier")
                var subjectIdentifier: String? = "",
                @SerializedName("Subject")
                var subject: String? = "",
                @SerializedName("ActionIdentifier")
                var actionIdentifier: String? = "",
                @SerializedName("Action")
                var action: String? = "",
                @SerializedName("Comment")
                var comment: String? = "",
                @SerializedName("NumericFactor")
                var numericFactor: String? = "",
                @SerializedName("TerminalDataID")
                var terminalDataID: String? = "",
                @SerializedName("TimeAccOff")
                var timeAccOff: String? = "",
                @SerializedName("GeoLocationID")
                var geoLocationID: String? = "",
                @SerializedName("GeoLocationDistanceMeter")
                var geoLocationDistanceMeter: String? = "",
                @SerializedName("DurationTimeAccOff")
                var durationTimeAccOff: String? = ""

        ) : Serializable

        class VehicleRouteStatus(
                @SerializedName("TerminalDataTimePeriodSecondNormal")
                var terminalDataTimePeriodSecondNormal: String? = "",
                @SerializedName("TerminalDataTimePeriodSecondMaximum")
                var terminalDataTimePeriodSecondMaximum: String? = "",
                @SerializedName("UserID")
                var userID: String? = "",
                @SerializedName("UserGroupIdentifierHighest")
                var userGroupIdentifierHighest: String? = "",
                @SerializedName("TerminalDataTimeFrom")
                var terminalDataTimeFrom: String? = "",
                @SerializedName("TerminalDataTimeTo")
                var terminalDataTimeTo: String? = "",
                @SerializedName("VehicleRouteMode")
                var vehicleRouteMode: String? = "",
                @SerializedName("MaximumConsiderableGPSVelocity")
                var maximumConsiderableGPSVelocity: String? = "",
                @SerializedName("GPSJitterCorrectionMinimumDistance")
                var gPSJitterCorrectionMinimumDistance: String? = "",
                @SerializedName("MinimumPitStopDuration")
                var minimumPitStopDuration: String? = "",
                @SerializedName("ProcessTimeBegin")
                var processTimeBegin: String? = "",
                @SerializedName("ProcessTimeDuration")
                var processTimeDuration: String? = ""

        ) : Serializable

    }

}