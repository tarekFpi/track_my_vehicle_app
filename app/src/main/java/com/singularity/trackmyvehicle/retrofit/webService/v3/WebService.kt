package com.singularity.trackmyvehicle.retrofit.webService.v3

import com.singularity.trackmyvehicle.model.apiResponse.v3.*
import com.singularity.trackmyvehicle.model.entity.SupportRequestCategory
import com.singularity.trackmyvehicle.repository.implementation.v3.NotificationResponseWrapper
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */

interface WebService {

    @FormUrlEncoded
    @POST("/")
    fun postLogin(
            @Field("_Script") script: String,
            @Field("UserSignInName") userSignInName: String,
            @Field("UserEmail") userEmail: String,
            @Field("UserPassword") userPassword: String
    ): Call<GenericResponse<UserResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun postLoginWithHash(
            @Field("_Script") script: String,
            @Field("UserSignInName") userSignInName: String,
            @Field("UserEmail") userEmail: String,
            @Field("UserPasswordHash") userPassword: String
    ): Call<GenericResponse<UserResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getTerminals(
            @Field("_Script") script: String
    ): Call<GenericResponse<TerminalListResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getUserProfile(
            @Field("_Script") script: String
    ): Call<GenericResponse<ProfileResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getUserProfileInfo(
            @Field("_Script") script: String,
            @Field("_Subject") subject: String,
            @Field("_Action") action: String
    ): Call<ProfileInfoResponse>

    @FormUrlEncoded
    @POST("/")
    fun getTerminalAggregatedData(
            @Field("_Script") script: String,
            @Field("TerminalDataMinutelyTimeFrom") from: String,
            @Field("TerminalDataMinutelyTimeTo") to: String
    ): Call<GenericResponse<TerminalAggregatedDataMinutelyResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getTerminalAggregatedDataByTerminalId(
            @Field("_Script") script: String,
            @Field("TerminalDataMinutelyTimeFrom") from: String,
            @Field("TerminalDataMinutelyTimeTo") to: String,
            @Field("TerminalID") terminalId: String
    ): Call<GenericResponse<TerminalAggregatedDataMinutelyResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getTerminalAggregatedGroupedData(
            @Field("_Script") script: String,
            @Field("TerminalDataMinutelyTimeFrom") from: String,
            @Field("TerminalDataMinutelyTimeTo") to: String,
            @Field("TerminalID") terminalId: Int,
            @Field("GroupLevel") groupLvl: Int
    ): Call<GenericResponse<TerminalAggregatedDataMinutelyResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getTerminalAggregatedGroupedDataAll(
            @Field("_Script") script: String,
            @Field("TerminalDataMinutelyTimeFrom") from: String,
            @Field("TerminalDataMinutelyTimeTo") to: String,
            @Field("GroupLevel") groupLvl: Int
    ): Call<GenericResponse<TerminalAggregatedDataMinutelyResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getTerminalDataMinutely(
            @Field("_Script") script: String,
            @Field("TerminalDataMinutelyTimeFrom") fromDateTime: String,
            @Field("TerminalDataMinutelyTimeTo") toDateTime: String,
            @Field("TerminalID") terminalId: String
    ): Call<GenericResponse<TerminalDataMinutelyResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun changePassword(
            @Field("_Script") script: String,
            @Field("Password") password: String,
            @Field("PasswordConfirm") passwordConfirm: String
    ): Call<GenericResponse<List<String>>>

    @FormUrlEncoded
    @POST("/")
    fun requestOtpPassword(
            @Field("_Script") script: String,
            @Field("UserSignInName") userSignInName: String,
            @Field("UserEmail") email: String
    ): Call<GenericResponse<OtpResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun resetPassword(
            @Field("_Script") script: String,
            @Field("OTPCode") otp: String,
            @Field("UserPassword") password: String,
            @Field("UserSignInName") userSignInName: String,
            @Field("UserEmail") email: String,
            @Field("OTPToken") token: String
    ): Call<GenericResponse<List<String>>>

    @FormUrlEncoded
    @POST("/")
    fun getMessages(
            @Field("_Script") script: String,
            @Field("MessageTimeFrom") from: String = "",
            @Field("RecordUpto") count: Int = 40,
            @Field("RecordFrom") offset: Int = 0,
            @Field("MessageContent") query: String = "",
            @Field("OrderBy") orderBy: String = "MessageTimeOrder",
            @Field("OrderDirection") orderDirection: String = "DESC",
            @Field("MessageIsRead") isRead: String? = null
    ): Call<GenericResponse<NotificationResponseWrapper>>

    @FormUrlEncoded
    @POST("/")
    fun getSupportRequestList(
            @Field("_Script") script: String,
            @Field("SupportRequestResponseTimeFrom") from: String,
            @Field("SupportRequestResponseTimeTo") to: String? = null
    ): Call<GenericResponse<List<SupportTicket>>>

    @FormUrlEncoded
    @POST("/")
    fun getSupportRequestCategoryList(
            @Field("_Script") script: String
    ): Call<GenericResponse<List<SupportRequestCategory>>>

    @FormUrlEncoded
    @POST("/")
    fun sendFCMNotification(
            @Field("_Script") script: String,
            @Field("FCMIdentifier") fcmToken: String
    ): Call<GenericResponse<List<String>>>    @FormUrlEncoded

    @POST("/")
    fun markMessageAsRead(
            @Field("MessageID") messageIds: String,
            @Field("MessageIsRead") messageIsRead: String = "1",
            @Field("_Script") script: String = ENDPOINTS.MESSAGE_READ
    ): Call<GenericResponse<List<Any>>>

    @FormUrlEncoded
    @POST("/")
    fun logout(
            @Field("_Script") script: String = ENDPOINTS.LOGOUT,
            @Field("FCMIdentifier") fcmToken: String
    ): Call<GenericResponse<Any>>

    @FormUrlEncoded
    @POST("/")
    fun createSupportRequest(
            @Field("_Script") script: String,
            @Field("SupportRequestSubject") subject: String,
            @Field("SupportRequestResponseMessage") message: String,
            @Field("SupportRequestCategoryIDList") category: String,
            @Field("TerminalID") terminalId: String,
            @Field("SupportRequestID") supportRequestID: String?
    ): Call<GenericResponse<List<Any>>>

    @FormUrlEncoded
    @POST("/")
    fun switchUser(
            @Field("_Script") script: String = "API/V1/User/Impersonate",
            @Field("UserEmail") userEmail: String
    ): Call<GenericResponse<Any>>

    @GET("/")
    fun fetchVehicleRoutesResponse(
            @Query("_Script") script : String,
            @Query("VehicleRouteMode") vehicleRouteMode : String,
            @Query("TerminalDataTimeFrom") terminalDataTimeFrom : String,
            @Query("TerminalDataTimeTo") terminalDataTimeTo : String,
            @Query("TerminalID") terminalId : String
    ) : Call<VehicleRouteTerminalData>?

}
