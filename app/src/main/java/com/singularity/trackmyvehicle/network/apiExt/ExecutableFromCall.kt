package com.singularity.trackmyvehicle.network.apiExt

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Sadman Sarar on 2019-04-10.
 */


open class ExecutableFromCall<T>(
        protected var call: Call<T>?,
        protected val mExecutor: AppExecutors,
        protected val onSuccess: ((T?) -> Unit)? = null,
        protected val onFailed: ((code: Int, msg: String, failedResponse: Response<T>?) -> Unit)? = null,
        protected val resourceForError: (() -> T?)? = null
) : Executable<Resource<T>, LiveData<Resource<T>>> {

    override fun executeAsync(): LiveData<Resource<T>> {
        val liveData = MutableLiveData<Resource<T>>()

        liveData.postValue(Resource.loading(resourceForError?.invoke()))

        doExecuteAsync(liveData)

        return liveData
    }

    protected fun doExecuteAsync(liveData: MutableLiveData<Resource<T>>) {

        mExecutor.networkThread {
            liveData.postValue(execute())
        }

    }

    private fun getResourceForSuccess(response: Response<T>): Resource<T> {
        val data = response.body()
        onSuccess?.invoke(data)
        return Resource.success(data)
    }


    private fun getResourceForError(t: Throwable? = null): Resource<T> {
        val msg = t?.message ?: "Something went wrong"
        onFailed?.invoke(500, msg, null)
        return Resource.error(msg, resourceForError?.invoke())
    }

    private fun getResourceForError(failedResponse: Response<T>): Resource<T> {
        val msg = failedResponse.parseErrorBody()
        onFailed?.invoke(failedResponse.code(), msg, failedResponse)
        return Resource.error(msg, resourceForError?.invoke())
    }

    @WorkerThread
    protected fun getApiCall(): Call<T>? {
        return call
    }

    override fun execute(): Resource<T> {
        return try {
            getApiCall()?.execute()?.let { response ->
                if (response.isSuccessful) {
                    getResourceForSuccess(response)
                } else {
                    getResourceForError(response)
                }
            } ?: getResourceForError()

        } catch (ex: Exception) {
            getResourceForError(ex)
        }
    }
}