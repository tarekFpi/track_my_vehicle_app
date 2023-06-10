package com.singularity.trackmyvehicle.network.apiExt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Sadman Sarar on 2019-04-11.
 */


interface ExecuteConverter<From, To> {
    fun convert(from: From): To
    fun convertAsync(from: From, callback: (to: To) -> Unit)
}

class CalledExecutor<From, T>(
        val data: From,
        executor: AppExecutors,
        private val converter: ExecuteConverter<From, Call<T>?>,
        onSuccess: ((T?) -> Unit)? = null,
        onFailed: ((code: Int, msg: String, failedResponse: Response<T>?) -> Unit)? = null,
        resourceForError: (() -> T?)? = null
) : ExecutableFromCall<T>(null, executor, onSuccess, onFailed, resourceForError) {

    override fun executeAsync(): LiveData<Resource<T>> {
        val liveData = MutableLiveData<Resource<T>>()

        liveData.postValue(Resource.loading(resourceForError?.invoke()))
        converter.convertAsync(data) {
            call = it
            doExecuteAsync(liveData)

        }
        return liveData

    }

    override fun execute(): Resource<T> {
        call = converter.convert(data)
        return super.execute()
    }
}

