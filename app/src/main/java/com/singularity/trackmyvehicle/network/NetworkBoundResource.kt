package com.singularity.trackmyvehicle.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread

/**
 * Created by Sadman Sarar on 8/3/18.
 * Awesome Helper class for data loading & managing events
 * link: https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample
 */
abstract class NetworkBoundResource<ResultType, RequestType> @MainThread
constructor() {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        val dbSource = loadFromDb()
        result.addSource(dbSource) { resultType ->
            result.removeSource(dbSource)
            if (shouldFetch(resultType)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { resultType -> result.value = Resource.success(resultType) }
            }
        }
    }

    fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) {
            resultType ->
            result.value = Resource.loading(resultType)
        }

        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)

            if (response!!.isSuccessful) {
                ioThread {
                    processResponse(response)?.let { saveCallResult(it) }
                    mainThread {
                        // we specially request a new live data,
                        // otherwise we will get immediately last cached value,
                        // which may not be updated with latest results received from network.
                        result.addSource(loadFromDb()
                        ) { resultType -> result.value = Resource.success(resultType) }
                    }
                }

            } else {
                onFetchFailed()
                result.addSource(dbSource
                ) { resultType -> result.value = response.errorMessage?.let { Resource.error(it, resultType) } }
            }
        }
    }

    protected open fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<ResultType>> {
        return result
    }

    @WorkerThread
    protected fun processResponse(response: ApiResponse<RequestType>): RequestType? {
        return response.body
    }

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>

}