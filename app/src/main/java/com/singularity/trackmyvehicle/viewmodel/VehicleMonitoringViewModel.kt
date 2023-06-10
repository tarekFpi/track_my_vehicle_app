package com.singularity.trackmyvehicle.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleCurrentStatusModel

/**
 * Created by Kariba Yasmin on 10/6/21.
 */
class VehicleMonitoringViewModel : ViewModel() {

    var mutableVehicleCurrentStatusList = MutableLiveData<List<VehicleCurrentStatusModel>>()

    var offlineVehiclesCount = 0
    var idleVehiclesCount = 0
    var movingVehiclesCount = 0
    var engineOffVehiclesCount = 0
    var allVehiclesCount = 0

    fun setCurrentVehicleStatus(vehiclesList: ArrayList<Terminal>, offlineVehicles: ArrayList<Terminal>, idleVehicles: ArrayList<Terminal>, movingVehicles: ArrayList<Terminal>, engineOffVehicles: ArrayList<Terminal>){
         offlineVehiclesCount = offlineVehicles.size
         idleVehiclesCount = idleVehicles.size
         movingVehiclesCount = movingVehicles.size
         engineOffVehiclesCount = engineOffVehicles.size
         allVehiclesCount = vehiclesList.size

        getVehicleCurrentStatusList()
    }

    fun getVehicleCurrentStatusList() : LiveData<List<VehicleCurrentStatusModel>> {
        val statusList : ArrayList<VehicleCurrentStatusModel> = ArrayList()

        statusList.add(VehicleCurrentStatusModel(0, allVehiclesCount, "All Vehicles"))
        statusList.add(VehicleCurrentStatusModel(1, movingVehiclesCount, "Moving"))
        statusList.add(VehicleCurrentStatusModel(2, idleVehiclesCount, "Idle"))
        statusList.add(VehicleCurrentStatusModel(3, engineOffVehiclesCount, "Engine Off"))
        statusList.add(VehicleCurrentStatusModel(4, offlineVehiclesCount, "Offline"))

        mutableVehicleCurrentStatusList.value = statusList

        return mutableVehicleCurrentStatusList
    }
}