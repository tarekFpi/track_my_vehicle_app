package com.singularity.trackmyvehicle.network.interceptor


import com.google.gson.Gson
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.utils.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-23.
 */


class CookieInterceptor @Inject constructor(
        private val mPrefRepo: PrefRepository,
        private val gson: Gson
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("iuyi","entered 1")
        val originalRequest = chain.request()

        var headerBuilder = originalRequest.headers()
                .newBuilder()
                .set("Accept", "application/json")

        if (mPrefRepo.cookie?.isNotEmpty() == true) {
            headerBuilder = headerBuilder
                    .set("Cookie", mPrefRepo.cookie)
        }

        val headers = headerBuilder.build()

        val newRequest = originalRequest.newBuilder().headers(headers).build()

        val response = chain.proceed(newRequest)

        //EventBus.getDefault().post(LogoutEvent(true,  response.request().url().encodedPath()))

        if (response.body()?.contentType()?.subtype() == "html" && mPrefRepo.userSource == UserSource.VERSION_3.identifier) {
            Log.d("iuyi","entered 2")
            EventBus.getDefault().post(LogoutEvent(true,  response.request().url().encodedPath()))
        }

        return response
    }

}