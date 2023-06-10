package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */

data class Error(
        @Expose
        @SerializedName("Code")
        var code: Int? = -10,
        @Expose
        @SerializedName("Description")
        var description: String? = "Description"
)