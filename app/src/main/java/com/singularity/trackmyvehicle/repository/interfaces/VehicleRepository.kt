package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.entity.VehicleRoutePolyline
import com.singularity.trackmyvehicle.network.Resource
import org.joda.time.DateTime

interface VehicleRepository {
    fun fetch(lazy: Boolean = false)
    /**
     * 0 -> Next
     * 1 -> Previous
     */
    fun selectNextVehicle(direction: Int = 0)
    fun getVehicles(maxCount: Int = 0): LiveData<List<Terminal>>
    fun getVehicleStatus(bstId: String): LiveData<Resource<VehicleStatus>>
    fun fetchVehicleRoutes(bstid: String, date: String): MutableLiveData<Resource<List<VehicleRoute>>>
    fun fetchCurrentRoutesVehicle(script: String, vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>>

    fun getCurrentVehicle(completion: (Terminal?) -> Unit)
    fun getVeicleRoutePolyLineAsync(bstId: String, date: DateTime, completion: (VehicleRoutePolyline?) -> Unit)
    fun getVeicleRoutePolyLine(bstId: String, date: DateTime): VehicleRoutePolyline?

    fun getTodaysTravelledAllDistance(): LiveData<List<TerminalAggregatedData>>
    fun getTodaysTravelledDistance(bId: String): LiveData<TerminalAggregatedData>
    fun fetchTodaysTravelledDistance(bId: String)
}
