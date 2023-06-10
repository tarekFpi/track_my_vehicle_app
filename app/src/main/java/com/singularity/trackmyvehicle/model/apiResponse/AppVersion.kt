package com.singularity.trackmyvehicle.model.apiResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Imran Chowdhury on 2020-01-29.
 */

data class AppVersion(
        @Expose
        @SerializedName("version")
        var version: String? = null
)