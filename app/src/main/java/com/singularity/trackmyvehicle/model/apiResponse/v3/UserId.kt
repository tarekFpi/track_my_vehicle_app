package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */


data class UserId(
        @Expose
        @SerializedName("ID")
        var userId: String? = null,
        @Expose
        @SerializedName("UserGroupIdentifierHighest")
        var userGroupIdentifierHighest: String? = null,
        @Expose
        @SerializedName("IsGuest")
        var isGuest: Boolean? = null
)