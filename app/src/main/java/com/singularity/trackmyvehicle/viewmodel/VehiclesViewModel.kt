package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.entity.VehicleRoutePolyline
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import org.joda.time.DateTime
import javax.inject.Inject


/**
 * Created by Sadman Sarar on 8/3/18.
 */
open class VehiclesViewModel
@Inject constructor(
        private val mRepository: VehicleRepository,
        private val mExecutors: AppExecutors,
        open val mPrefRepository: PrefRepository
) : ViewModel() {

    fun fetch(lazy: Boolean = false) {
        mRepository.fetch(lazy)
    }

    fun getVehicles(maxCount: Int = 0): LiveData<List<Terminal>> {
        return mRepository.getVehicles(maxCount)
    }

    fun getVehiclesFromLocal(): LiveData<List<Terminal>> {
        return mRepository.getVehicles()
    }

    private fun getVehicleStatus(bstId: String): LiveData<Resource<VehicleStatus>> {
        return mRepository.getVehicleStatus(bstId)
    }

    fun getCurrentVehicleStatus(): LiveData<Resource<VehicleStatus>>? {
        val currentVehicle = mPrefRepository.currentVehicle()
//        return if (currentVehicle == "") null else getVehicleStatus(currentVehicle)
        return getVehicleStatus(currentVehicle)
    }

    fun changeCurrentVehicle(bstid: String, vrn: String, bId: String) {
        mPrefRepository.changeCurrentVehicle(bstid, vrn, bId)
    }

    fun changeCurrentVehicle(bstid: String, vrn: String, bId: String, location: String) {
        mPrefRepository.changeCurrentVehicle(bstid, vrn, bId, location)
    }

    fun getVehicleRoutes(bstid: String,
                         date: String): MutableLiveData<Resource<List<VehicleRoute>>> {
        return mRepository.fetchVehicleRoutes(bstid, date)
    }

    fun getCurrentVehicleRoutes(date: String): MutableLiveData<Resource<List<VehicleRoute>>> {
        return mRepository.fetchVehicleRoutes(mPrefRepository.currentVehicle(), date)
    }

    fun getCurrentRouteVehicle(script: String, vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>> {
        return mRepository.fetchCurrentRoutesVehicle(script, vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
    }

    open fun getCurrentVehicle(completion: (Terminal?) -> Unit) {
        mRepository.getCurrentVehicle(completion)
    }

    fun getVeicleRoutePolyLine(bstId: String, date: DateTime,
                               completion: (VehicleRoutePolyline?) -> Unit) {
        mRepository.getVeicleRoutePolyLineAsync(bstId, date, completion)
    }

    fun getTodaysTravelledDistance(bId: String): LiveData<TerminalAggregatedData> {
        return mRepository.getTodaysTravelledDistance(bId)
    }

    fun getTodaysTravelledDistance(): LiveData<List<TerminalAggregatedData>> {
        return mRepository.getTodaysTravelledAllDistance()
    }

    fun fetchTodaysTravelledDistance(bId: String) {
        mRepository.fetchTodaysTravelledDistance(bId)

    }

    fun selectNextVehicle() {
        mRepository.selectNextVehicle()
    }

    fun selectPreviousVehicle() {
        mRepository.selectNextVehicle(1)
    }
}