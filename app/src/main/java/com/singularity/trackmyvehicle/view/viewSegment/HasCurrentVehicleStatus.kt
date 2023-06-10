package com.singularity.trackmyvehicle.view.viewSegment

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status

interface HasCurrentVehicleStatus {
    var mCurrentVehicleStatusLiveData: LiveData<Resource<VehicleStatus>>?

    fun currentVehicleStatusObserver(): Observer<Resource<VehicleStatus>> {
        return Observer { data ->
            if (data == null)
                return@Observer
            when (data.status) {
                Status.SUCCESS -> {
                    updateCurrentVehicleStatus(data.data)
                    setProgressVisibility(View.GONE)
                }
                Status.LOADING -> {
                    setProgressVisibility(View.VISIBLE)
                }
                Status.ERROR   -> {
                    setProgressVisibility(View.GONE)
                }
            }
        }
    }

    fun updateCurrentVehicleStatus(data: VehicleStatus?)
    fun setProgressVisibility(visibility: Int)
}