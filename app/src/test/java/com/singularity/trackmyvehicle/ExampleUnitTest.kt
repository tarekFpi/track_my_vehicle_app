package com.singularity.trackmyvehicle

import android.content.Context
import com.google.gson.stream.MalformedJsonException
import com.singularity.trackmyvehicle.di.AppModule
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleRouteResponse
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private lateinit var mApi: WebService

    private var mApiToken: String = ""

   companion object {
        private lateinit var context: Context
        fun setContext(con: Context) {
            context=con
        }
    }

    @Before
    fun setUp() {

        val appModule = AppModule()
        mApi = appModule.provideGithubService(appModule.provideOkHttpClientV2(context,appModule.provideHttpInterceptro()))

        mApiToken = mApi.postLogin(
                "gazitv",
                "11235813213455",
                "", "android", "")
                .execute()
                ?.body()
                ?.accessToken ?: ""
    }

    @Test
    fun test_login() {
        var response: Response<LoginResponse>? = null

        val millis = measureTimeMillis {
            response = mApi.postLogin(
                    "gazitv",
                    "11235813213455",
                    "", "android", "")
                    .execute()
        }

        System.out.println(millis)

        mApi.logout(response?.body()?.accessToken ?: "")
    }

    @Test
    fun addition_isCorrect() {


        val module = AppModule()

        val api = module.provideGithubService(module.provideOkHttpClientV2(context,module.provideHttpInterceptro()))

        val response = api.postLogin("demoCorp", "11235813213455", "", "android", "").execute()

        assertTrue(response.isSuccessful)

    }

    fun test_api() {

        val bstIds = mApi.fetchVehicleList("1", mApiToken, 100)
                .execute()
                ?.body()
                ?.data
                ?.map {
                    it.bstid
                }

        val times = HashMap<String, Long>()

        bstIds?.take(2)?.forEach {
            var response: Response<GenericApiResponse<VehicleRouteResponse>>
            val millis = measureTimeMillis {
                try {

                    val req = mApi.fetchVehicleRoutes(it, DateTime.now().toString("yyyy-MM-dd"), mApiToken)
                            .request()

                } catch (ex: MalformedJsonException) {
                    ex.stackTrace
                }
            }
            times[it] = millis
        }

        times.forEach { bstId, millis ->
            System.out.println("$bstId -> $millis")
        }
    }


    @After
    fun tearDown() {
        mApi.logout(mApiToken).execute()
    }
}
