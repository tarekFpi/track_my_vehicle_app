package com.singularity.trackmyvehicle.model.apiResponse.v3
import com.google.gson.annotations.SerializedName


/**
 * Created by Sadman Sarar on 2020-02-26.
 */
data class OtpResponse(
    @SerializedName("Code")
    var code: Int? = null,
    @SerializedName("Expiry")
    var expiry: String? = null,
    @SerializedName("Token")
    var token: String? = null
)

data class OtpResponseWrapper(
        @SerializedName("OTP")
        var otp: OtpResponse? = null
)