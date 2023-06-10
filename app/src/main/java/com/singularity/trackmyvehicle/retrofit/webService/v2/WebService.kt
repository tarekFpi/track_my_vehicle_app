package com.singularity.trackmyvehicle.retrofit.webService.v2

import androidx.lifecycle.LiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.*
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.*
import com.singularity.trackmyvehicle.network.ApiResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Sadman Sarar on 8/3/18.
 * Retrofit Service class
 */
interface WebService {

    @GET("/vehicles")
    fun getVehicle(): LiveData<ApiResponse<List<Vehicle>>>

    @FormUrlEncoded
    @POST("/app2018/login.php")
    fun postLogin(
            @Field("username") username: String,
            @Field("password") password: String,
            @Field("deviceid") deviceid: String,
            @Field("devicetype") devicetype: String,
            @Field("fcm") fcm: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/vehicle_list.php")
    fun fetchVehicleList(
            @Field("page") page: String,
            @Field("access_token") accessToken: String,
            @Field("per_page") perPage: Int
    ): Call<PaginatedVehicleWrapper>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/vehicle_status.php")
    fun fetchVehicleStatus(
            @Field("bstid") bstid: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<VehicleStatus>>

    @FormUrlEncoded
    @Headers("Accept: application/json",
            "Accept-Encoding: gzip, defalte, br")
    @POST("/app2018/vehicle_route.php")
    fun fetchVehicleRoutes(
            @Field("bstid") bstid: String,
            @Field("date") date: String, // 2018-03-11
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<VehicleRouteResponse>>

    @GET("/")
    fun fetchVehicleRoutesResponse(
            @Query("_Script") script : String,
            @Query("VehicleRouteMode") vehicleRouteMode : String,
            @Query("TerminalDataTimeFrom") terminalDataTimeFrom : String,
            @Query("TerminalDataTimeTo") terminalDataTimeTo : String,
            @Query("TerminalID") terminalId : String,
            @Field("access_token") accessToken: String
    ) : Call<VehicleRouteTerminalData>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/distance_report.php")
    fun fetchDistanceReport(
            @Field("bstid") bstid: String,
            @Field("date") date: String, // 2018-03-11
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<DistanceReportResponse>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/speed_report.php")
    fun fetchSpeedReport(
            @Field("bstid") bstid: String,
            @Field("date") date: String, // 2018-03-11
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<SpeedReportResponse>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/expense_header.php")
    fun fetchExpenseHeader(
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<List<ExpenseHeader>>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/expense_add.php")
    fun postExpense(
            @Field("bstid") bstid: String,
            @Field("date") date: String, // 2018-03-11
            @Field("expense") expense: String,
            @Field("amount") amount: Int,
            @Field("details") details: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/user_profile.php")
    fun fetchUserInfo(
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<Profile>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/request_otp.php")
    fun requestForgetPasswordApi(
            @Field("username") username: String,
            @Field("mobile") mobile: String
    ): Call<OtpResponse>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/expense_fetch.php")
    fun fetchPreviousExpense(
            @Field("bstid") bstid: String,
            @Field("date") date: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<List<Expense>>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/validate_otp.php")
    fun validateOtp(
            @Field("otp") otp: String,
            @Field("otp_token") otpToken: String
    ): Call<OtpValidationResponse>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/forget_password_change.php")
    fun resetPassword(
            @Field("username") username: String,
            @Field("password_token") passwordToken: String,
            @Field("password") password: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/change_password.php")
    fun changePassword(
            @Field("username") username: String,
            @Field("old_password") currentPassword: String,
            @Field("new_password") newPassword: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/feedback_header.php")
    fun fetchFeedbackHeaders(
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<List<FeedbackHeader>>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/feedback_add.php")
    fun createFeedback(
            @Field("bstid") bstid: String,
            @Field("feedback") feedback: String,
            @Field("other_details") other_details: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/feedback_fetch.php")
    fun fetchFeedback(
            @Field("page") page: String,
            @Field("per_page") per_page: String,
            @Field("access_token") accessToken: String
    ): Call<PaginatedWrapper<List<Feedback>>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/feedback_remarks_fetch.php")
    fun fetchFeedbackRemarks(
            @Field("feedback_id") feedbackId: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<List<FeedbackRemark>>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/fcm_refresh.php")
    fun sendFCMNotification(
            @Field("fcm") fcmToken: String,
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/logout.php")
    fun logout(
            @Field("access_token") accessToken: String
    ): Call<GenericApiResponse<String>>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("/app2018/secure_mode.php")
    fun secureMode(
            @Field("access_token") accessToken: String,
            @Field("password") password: String,
            @Field("bstid") bstid: String,
            @Field("secure") secure: String
    ): Call<GenericApiResponse<SecureModeReponse>>
}