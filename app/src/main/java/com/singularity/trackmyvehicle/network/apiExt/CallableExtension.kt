package com.singularity.trackmyvehicle.network.apiExt

import androidx.lifecycle.LiveData
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Sadman Sarar on 2019-04-10.
 */
fun <T> Call<T>.asExecutable(
        executor: AppExecutors,
        onSuccess: ((T?) -> Unit)? = null,
        onFailed: ((code: Int, msg: String, failedResponse: Response<T>?) -> Unit)? = null
): Executable<Resource<T>, LiveData<Resource<T>>> {
    return ExecutableFromCall(
            this,
            executor,
            onSuccess,
            onFailed)

}

fun <From, T> convertToCalledExecutor(
        data: From,
        executor: AppExecutors,
        converter: ExecuteConverter<From, Call<T>?>,
        onSuccess: ((T?) -> Unit)? = null,
        onFailed: ((code: Int, msg: String, failedResponse: Response<T>?) -> Unit)? = null,
        resourceForError: (() -> T?)? = null
): CalledExecutor<From, T> {

    return CalledExecutor(
            data,
            executor,
            converter,
            onSuccess,
            onFailed,
            resourceForError
    )
}


