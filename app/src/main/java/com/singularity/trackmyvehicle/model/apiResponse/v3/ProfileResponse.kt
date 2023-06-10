package com.singularity.trackmyvehicle.model.apiResponse.v3
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by Imran Chowdhury on 2020-01-26.
 */

data class ProfileResponseWrapper(
        @Expose
        @SerializedName("User")
        var profile: ProfileResponse? = null
)


data class ProfileResponse(
    @Expose
    @SerializedName("UserID")
    var userID: String?,
    @Expose
    @SerializedName("UserEmail")
    var userEmail: String?,
    @Expose
    @SerializedName("UserSignInName")
    var userSignInName: String?,
    @Expose
    @SerializedName("UserPhone")
    var userPhone: String?,
    @Expose
    @SerializedName("UserGroupIdentifier")
    var userGroupIdentifier: String?
)