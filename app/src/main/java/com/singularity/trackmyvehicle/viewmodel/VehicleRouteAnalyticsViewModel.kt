package com.singularity.trackmyvehicle.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.internal.fa
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.EventsVehicleRouteAnalyticsItem
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.utils.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Kariba Yasmin on 8/22/21.
 */
class VehicleRouteAnalyticsViewModel : ViewModel(){

    var mutableEventsListData = MutableLiveData<ArrayList<EventsVehicleRouteAnalyticsItem>>()
    var mutableMotionStateListData = MutableLiveData<ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>>()

    var checked : Boolean = false

    var cookie : String = ""

    var vehicleRouteMode : String = ""

    var terminalDataTimeFrom : String = ""

    var terminalDataTimeTo : String = ""

    var terminalId : String = ""

   fun loadEventsListData(context: Context) : LiveData<ArrayList<EventsVehicleRouteAnalyticsItem>>{
       mutableEventsListData = MutableLiveData()
       getMutableEventData(context)
       return mutableEventsListData
   }

   fun loadMotionStateData(context: Context) : LiveData<ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>>{
       mutableMotionStateListData = MutableLiveData()
       getMutableMotionStateData()
       return mutableMotionStateListData
   }

    private fun getMutableMotionStateData() {
        val call = RetrofitClient.getInstance(cookie)?.create(ApiHandler::class.java)?.getVehicleRouteResponse(
                ENDPOINTS.FETCH_ROUTE_VEHICLE,
                vehicleRouteMode,
                terminalDataTimeFrom,
                terminalDataTimeTo,
                terminalId)
        call?.enqueue(object : Callback<VehicleRouteTerminalData> {
            override fun onResponse(call: Call<VehicleRouteTerminalData>, response: Response<VehicleRouteTerminalData>) {
                if (response.isSuccessful && response.body() != null) {
                    setVehicleRouteAnalyticsMotionStateData(response?.body()?.vehiclesRouteSuccessResponse?.vehicleRouteMotionState as ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>)
                }
            }

            override fun onFailure(call: Call<VehicleRouteTerminalData>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun getMutableEventData(context: Context) {
        var eventList : ArrayList<EventsVehicleRouteAnalyticsItem> = ArrayList()

        eventList.add(EventsVehicleRouteAnalyticsItem(1, context.getString(R.string.parking), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(2, context.getString(R.string.sudden_acceleration), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(3, context.getString(R.string.harsh_break), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(4, context.getString(R.string.speed_violation), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(5, context.getString(R.string.engine_start_stop), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(6, context.getString(R.string.idle_flag), false))
        eventList.add(EventsVehicleRouteAnalyticsItem(7, context.getString(R.string.power_down), false))

        setEventsListData(eventList)
    }
    fun getVehicleRouteFetchParameterData(context: Context, ck: String, veRouteMode: String, terminalTimeFrom: String, terminalTimeTo: String, trId: String){
        cookie = ck
        vehicleRouteMode = veRouteMode
        terminalDataTimeFrom = terminalTimeFrom
        terminalDataTimeTo = terminalTimeTo
        terminalId = trId

    }

    fun getChecked(context: Context, checkedEvent: Boolean){
        checked = checkedEvent
    }

    private fun setEventsListData(data: ArrayList<EventsVehicleRouteAnalyticsItem>){
        mutableEventsListData.value = data
    }

    private fun setVehicleRouteAnalyticsMotionStateData(data: ArrayList<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteMotionState>){
        mutableMotionStateListData.value = data
    }

}