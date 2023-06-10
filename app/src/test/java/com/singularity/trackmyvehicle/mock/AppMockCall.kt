package com.singularity.trackmyvehicle.mock

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Created by Imran Chowdhury on 8/12/2018.
 */


abstract class AppMockCall<T> : Call<T> {
    override fun enqueue(callback: Callback<T>?) {
    }

    override fun isExecuted(): Boolean {
        return false
    }

    override fun clone(): Call<T> {
        return this
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun cancel() {
    }

    override fun execute(): Response<T> {
        throw SocketTimeoutException()
    }

    override fun request(): Request? {
        return null
    }

}