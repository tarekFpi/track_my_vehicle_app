package com.singularity.trackmyvehicle.network

import com.singularity.trackmyvehicle.model.apiResponse.v2.PaginatedWrapper
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/11/18.
 */

class PaginatedFeedbackFetcher
@Inject constructor(
        private val mApi: WebService,
        private val mPrefRepository: PrefRepository

) {

    var data = ArrayList<Feedback>()

    var currentPage = 1

    var lastPage = - 999

    var isRunning = false

    var callback: PaginatedCallback<Feedback>? = null

    fun fetch(page: Int = 1) {

        isRunning = true

        mApi.fetchFeedback(
                page.toString(),
                30.toString(),
                mPrefRepository.apiToken()
        ).enqueue(object : Callback<PaginatedWrapper<List<Feedback>>> {

            override fun onResponse(call: Call<PaginatedWrapper<List<Feedback>>>?, response: Response<PaginatedWrapper<List<Feedback>>>?) {
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
                fetchEnd()
                fetchSuccess()
            }

            override fun onFailure(call: Call<PaginatedWrapper<List<Feedback>>>?, t: Throwable?) {
                fetchFail()
                fetchEnd()
            }
        })
    }

    private fun fetchSuccess() {
        callback?.fetchEnded(data)
    }

    private fun fetchFail() {
        callback?.fetchFailed("")
    }

    private fun fetchEnd() {
        isRunning = false
    }


}
