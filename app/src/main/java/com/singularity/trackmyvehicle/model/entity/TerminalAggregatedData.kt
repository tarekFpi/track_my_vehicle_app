package com.singularity.trackmyvehicle.model.entity
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime


@Entity(tableName = "terminal_aggregated_data",
        primaryKeys = ["terminalID", "aggregatedDurationType", "aggregatedDurationStartFrom"],
        indices = [Index(
                value = ["terminalID", "aggregatedDurationType", "aggregatedDurationStartFrom"])])
data class TerminalAggregatedData(
    @SerializedName("TerminalDataMinutelyTimeFrom")
    var terminalDataMinutelyTimeFrom: String? = "",
    @SerializedName("TerminalDataMinutelyTimeTo")
    var terminalDataMinutelyTimeTo: String? = "",
    @SerializedName("TerminalDataMinutelyCount")
    var terminalDataMinutelyCount: String? = "",
    @SerializedName("TerminalDataMinutelyDistanceMeter")
    var terminalDataMinutelyDistanceMeter: String? = "",
    @SerializedName("TerminalID")
    var terminalID: String = "",
    @SerializedName("ProviderCode")
    var providerCode: String? = "",
    @SerializedName("ProviderName")
    var providerName: String? = "",
    @SerializedName("TerminalAssignmentCode")
    var terminalAssignmentCode: String? = "",
    @SerializedName("TerminalAssignmentIsSuspended")
    var TerminalAssignmentIsSuspended: String? = "",
    @SerializedName("CustomerCode")
    var customerCode: String? = "",
    @SerializedName("CustomerName")
    var customerName: String? = "",
    @SerializedName("CarrierRegistrationNumber")
    var carrierRegistrationNumber: String? = "",
    @SerializedName("CarrierName")
    var carrierName: String? = "",
    @SerializedName("CarrierTypeName")
    var carrierTypeName: String? = "",
    var aggregatedDurationType :String = "",
    var aggregatedDurationStartFrom :DateTime = DateTime.now()
)