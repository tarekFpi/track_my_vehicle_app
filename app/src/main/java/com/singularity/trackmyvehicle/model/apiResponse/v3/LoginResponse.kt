package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */

data class UserResponseWrapper(
        @Expose
        @SerializedName("User")
        var login: LoginResponse? = null)
data class TerminalListResponseWrapper(
        @Expose
        @SerializedName("Terminal")
        var data: List<Terminal>? = null)

data class LoginResponse(
        @Expose
        @SerializedName("UserID")
        var userId: String? = null,
        @Expose
        @SerializedName("UserEmail")
        var userEmail: String? = null,
        @Expose
        @SerializedName("UserSignInName")
        var userSignInName: String? = null,
        @Expose
        @SerializedName("UserPasswordHashSalt")
        var userPasswordHashSalt: String? = null,
        @Expose
        @SerializedName("UserName")
        var userName: String? = null,
        @Expose
        @SerializedName("UserPhone")
        var userPhone: String? = null,
        @Expose
        @SerializedName("UserGroupIdentifier")
        var userGroupIdentifier: String? = null,
        @Expose
        @SerializedName("UserGroupName")
        var userGroupName: String? = null,
        @Expose
        @SerializedName("UserGroupWeight")
        var userGroupWeight: String? = null
)