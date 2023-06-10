package com.singularity.trackmyvehicle.network.apiExt

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.model.apiResponse.v2.ErrorResponse
import retrofit2.Response

fun <T> Response<T>.parseErrorBody(): String {
    var userMsg = "Something went wrong"
    try {
        val gson = Gson()
        val errorString = this.errorBody()?.string()
        val error = gson.fromJson(errorString, ErrorResponse::class.java)
        if (error.userMessage == null) {
            val genError =
                gson.fromJson(errorString, GenericErrorResponse::class.java)
            userMsg = genError.userMessage ?: userMsg
        } else {
            userMsg = error.userMessage ?: userMsg
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return userMsg
}

fun parseErrorBody(): String {
    return "Something went wrong"
}

data class GenericErrorResponse(
    @SerializedName("app_message")
    var appMessage: String? = null,
    @SerializedName("code")
    var code: Int? = null,
    @SerializedName("user_message")
    var userMessage: String? = null

)