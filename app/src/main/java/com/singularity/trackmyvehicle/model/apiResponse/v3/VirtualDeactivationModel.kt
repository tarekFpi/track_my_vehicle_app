package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kariba Yasmin on 3/15/21.
 */
data class VirtualDeactivationModel (
        @SerializedName("Error")
        var error : ErrorVirtualDeactivationResponse? = null,
        @SerializedName("User")
        var user : UserVirtualDeactivationResponse? = null,
        @SerializedName("Response")
        var response : VirtualDeactivationResponse? = null
): Serializable

data class ErrorVirtualDeactivationResponse(
        @SerializedName("Code")
       var code : Int? = 0,
        @SerializedName("Description")
       var description : String? = ""
):Serializable

data class UserVirtualDeactivationResponse(
        @SerializedName("ID")
        var iD : Int? = 0,
        @SerializedName("GroupIdentifierHighest")
        var groupIdentifierHighest : String? = "",
        @SerializedName("Name")
        var name : String? = ""

):Serializable

data class VirtualDeactivationResponse (
        @SerializedName("Status")
        var status : String? =""
): Serializable