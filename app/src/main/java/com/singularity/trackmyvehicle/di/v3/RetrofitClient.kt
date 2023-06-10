package com.singularity.trackmyvehicle.di.v3

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.network.interceptor.CookieInterceptor
import com.singularity.trackmyvehicle.network.parser.DateTimeParser
import com.singularity.trackmyvehicle.retrofit.LiveDataCallAdapterFactory
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.annotations.NotNull
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Kariba Yasmin on 3/7/21.
 */
object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null

    fun getInstance(cookie: String): Retrofit? {
        okHttpClient = null
        initOkHttp(cookie)

        retrofit = null
        retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_V3)
                .client(okHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit
    }

    private fun initOkHttp(cookie: String) {
        val REQUEST_TIMEOUT = 30
        val httpClient: OkHttpClient.Builder = OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)

        // TODO: Enable only for testing
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(interceptor)

        httpClient.addInterceptor(object : Interceptor {
            @NotNull
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val original: Request = chain.request()
                val requestBuilder: Request.Builder = original.newBuilder()
                        .addHeader("Cookie", "$cookie")
                        .addHeader("Accept", "application/json")

                val request: Request = requestBuilder.build()
                return chain.proceed(request)
            }
        })
        okHttpClient = httpClient.build()
    }
}
