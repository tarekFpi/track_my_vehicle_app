package com.singularity.trackmyvehicle.di.v3

import com.singularity.trackmyvehicle.model.apiResponse.v3.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Kariba Yasmin on 3/9/21.
 */
interface ApiHandler {

    @FormUrlEncoded
    @POST("/")
    fun createVirtualActivation(
            @Field("_Script") script : String,
            @Field("TerminalID") terminalId: Int,
            @Field("ParkingDurationMinute") minute: Int,
            @Field("ParkingRadiusMeter") distance: Int
    ) : Call<VirtualWatchmanActivationModel>


    @GET("/")
    fun getParkingList(
            @Query("_Script") script : String,
            @Query("TerminalID") terminalId: Int,
            @Query("ParkingIsActive") parkingIsActive: Int
    ) : Call<VirtualActivationParkingListModel>

    @GET("/")
    fun getTripReportList(
            @Query("_Script") script : String,
            @Query("TerminalID") terminalId: String,
            @Query("TripTimeFrom") tripTimeFrom: String,
            @Query("TripTimeTo") tripTimeTo : String
    ) : Call<TripReportResponse>

    @GET("/")
    fun getAnalyticsResponse(
            @Query("_Script") script : String
    ) : Call<AnalyticsResponse>

    @GET("/")
    fun getVehicleRouteResponse(
            @Query("_Script") script : String,
            @Query("VehicleRouteMode") vehicleRouteMode : String,
            @Query("TerminalDataTimeFrom") terminalDataTimeFrom : String,
            @Query("TerminalDataTimeTo") terminalDataTimeTo : String,
            @Query("TerminalID") terminalId : String
    ) : Call<VehicleRouteTerminalData>

    @FormUrlEncoded
    @POST("/")
    fun deactivateVirtualActivation(
            @Field("_Script") script : String,
            @Field("TerminalID") terminalId: Int
    ): Call<VirtualDeactivationModel>

    @Multipart
    @POST("/")
    fun profileNameUpdate(
            @Query("_Script") script: String,
            @Query("_Subject") subject: String,
            @Query("_Action") action: String,
            @Part("NameFirst") nameFirst : RequestBody,
            @Part("NameMiddle") nameMiddle : RequestBody,
            @Part("NameLast") nameLast : RequestBody
    ): Call<ProfileInfoResponse>
}