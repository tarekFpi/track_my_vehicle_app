package com.singularity.trackmyvehicle.network

import com.singularity.trackmyvehicle.model.apiResponse.v2.PaginatedVehicleWrapper
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/11/18.
 */

class PaginatedVehicleFetcher
@Inject constructor(
        private val mApi: WebService,
        private val mPrefRepository: PrefRepository

) {

    var data = ArrayList<Vehicle>()

    var currentPage = 1

    var lastPage = - 999

    var isRunning = false

    private var mLastFetchedTime: DateTime? = null

    var callback: PaginatedCallback<Vehicle>? = null

    fun fetch(page: Int = 1) {
        isRunning = true

        mApi.fetchVehicleList(
                page.toString(),
                mPrefRepository.apiToken(),
                30
        ).enqueue(object : Callback<PaginatedVehicleWrapper> {

            override fun onResponse(call: Call<PaginatedVehicleWrapper>?, response: Response<PaginatedVehicleWrapper>?) {
                if (response?.isSuccessful == false) {
                    fetchFail()
                    fetchEnd()
                    return
                }
                currentPage = response?.body()?.meta?.currentPage ?: currentPage
                lastPage = response?.body()?.meta?.lastPage ?: lastPage
                data.addAll(response?.body()?.data ?: ArrayList())
                if (currentPage < lastPage) {
                    fetch(currentPage + 1)
                    isRunning = true
                    return
                }
                fetchSuccess()
                fetchEnd()
            }

            override fun onFailure(call: Call<PaginatedVehicleWrapper>?, t: Throwable?) {
                fetchFail()
                fetchEnd()
            }
        })
    }

    private fun fetchSuccess() {
        callback?.fetchEnded(data)
        data = ArrayList<Vehicle>()
        mLastFetchedTime = DateTime.now()
    }

    private fun fetchFail() {
        callback?.fetchFailed("")
        data = ArrayList<Vehicle>()
    }

    private fun fetchEnd() {
        isRunning = false
        data = ArrayList<Vehicle>()
    }

    fun shouldFetchLazy(): Boolean {
        return (DateTime.now().millisOfSecond - (mLastFetchedTime?.millis ?: 0)) > 5 * 60 * 1000
    }

}

interface PaginatedCallback<T> {
    fun fetchEnded(data: List<T>)
    fun fetchFailed(msg: String)
}