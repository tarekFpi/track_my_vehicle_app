package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData


data class TerminalAggregatedDataMinutelyResponseWrapper(
        @Expose
        @SerializedName("TerminalDataMinutely")
        var data: List<TerminalAggregatedData>? = null
)
