package com.singularity.trackmyvehicle.retrofit

import com.singularity.trackmyvehicle.model.apiResponse.AppVersion
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by Imran Chowdhury on 2020-01-29.
 */


interface MiddleWebService {
    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/api/bstinfo")
    fun fetchUserVersion(
            @Field("query_data") encryptedUserName: String
    ): Call<GenericApiResponse<AppVersion>>
}