package com.singularity.trackmyvehicle.retrofit

import android.content.Context
import com.singularity.trackmyvehicle.di.AppModule
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

/**
 * Created by Sadman Sarar on 4/24/2018.
 */
class WebServiceTest {

    companion object {

       /* private lateinit var context: Context
        fun setContext(con: Context) {
            context=con
        }*/
        private const val MAX_API_CALL_TIMEOUT = 5000
        private const val TEST_USERNAME = "gazitv"
    }

    val mModule = AppModule()
   // val mApi = mModule.provideGithubService(mModule.provideOkHttpClientV2(mModule.provideHttpInterceptro()))




    private var mApiToken: String = ""

    @Before
    fun setUp() {

        //mApiToken = getApiToken()
    }

    @Test
    fun testNotNull() {
        //Assert.assertNotNull(mApi)
    }

    /*
    @Test
    fun test_login() {
        var response: Response<LoginResponse>? = null
        val millis = measureTimeMillis {
            response = mApi.postLogin(
                    TEST_USERNAME,
                    "11235813213455",
                    UUID.randomUUID().toString(),
                    "android",
                    "")
                    .execute()

        }
        assertNotNull(response)
        assertTrue(response?.isSuccessful!!)
        assertNotNull(response?.body()?.accessToken)
        assertFalse(response?.body()?.accessToken?.isEmpty()!!)

        print("Time Taken: $millis")

        assertTrue(millis < MAX_API_CALL_TIMEOUT)
    }


    @Test
    fun test_fetchVehicleList() {
        var response: Response<PaginatedWrapper<List<Vehicle>>>? = null
        val accessToken = mApiToken
        val millis = measureTimeMillis {
            response = mApi.fetchVehicleList("1", accessToken, 30).execute()
        }

        assertNotNull(response)
        assertTrue(response?.isSuccessful!!)
        val data = response?.body()?.data
        assertNotNull(data)
        assertTrue(data?.isNotEmpty()!!)

        print("Time Taken: $millis")

        assertTrue(millis < MAX_API_CALL_TIMEOUT)

    }

    @Test
    fun test_fetchVehicleStatus() {
        var response: Response<GenericApiResponse<VehicleStatus>>? = null
        val accessToken = mApiToken
        val bstIds = mApi.fetchVehicleList("1", accessToken, 30)
                .execute()
                .body()
                ?.data?.map {
            it.bstid
        }
        assertTrue(bstIds?.isNotEmpty()!!)
        bstIds.forEach {
            val millis = measureTimeMillis {
                response = mApi.fetchVehicleStatus(it, accessToken).execute()
            }

            assertNotNull(response)
            assertTrue(response?.isSuccessful!!)
            val data = response?.body()?.data
            assertNotNull(data)
            assertTrue(data?.bstid == it)

            println("BST ID: $it with time Taken: $millis")

            assertTrue(millis < MAX_API_CALL_TIMEOUT)
        }


    }

    @Test
    fun test_fetchVehicleRoutes() {
        var testTime = HashMap<String, Long>()
        var response: Response<GenericApiResponse<VehicleRouteResponse>>? = null
        val accessToken = mApiToken
        val bstIds = mApi.fetchVehicleList("1", accessToken, 30)
                .execute()
                .body()
                ?.data?.map {
            it.bstid
        }
        assertTrue(bstIds?.isNotEmpty()!!)
        bstIds.forEach {
            val millis = measureTimeMillis {
                response = mApi.fetchVehicleRoutes(it, DateTime.now().toString("yyyy-MM-dd"), accessToken).execute()
            }

            assertNotNull(response)
            val data = response?.body()?.data
            System.out.println(response?.code())
            if (response?.code() != 403) {
                assertNotNull(data)
                assertTrue(data?.route?.isNotEmpty()!!)
                assertTrue(data.vehicle?.bstid == it)
            }

            testTime[it] = millis
        }
        testTime.forEach { bstId, time ->
            System.out.println("$bstId -> $time")
        }

    }

    @Test
    fun test_fetchDistanceReport() {
        var testTime = HashMap<String, Long>()
        var response: Response<GenericApiResponse<DistanceReportResponse>>? = null
        val accessToken = mApiToken
        val bstIds = mApi.fetchVehicleList("1", accessToken, 30)
                .execute()
                .body()
                ?.data?.map {
            it.bstid
        }
        assertTrue(bstIds?.isNotEmpty()!!)
        bstIds.forEach {
            val millis = measureTimeMillis {
                response = mApi.fetchDistanceReport(it, DateTime.now().toString("yyyy-MM-dd"), accessToken).execute()
            }

            assertNotNull(response)
            val data = response?.body()?.data
            assertTrue(response?.isSuccessful!!)
            assertNotNull(data)
            assertTrue(data?.distance?.isNotEmpty()!!)
            assertTrue(data.vehicle?.bstid == it)

            testTime[it] = millis
        }
        testTime.forEach { bstId, time ->
            System.out.println("$bstId -> $time")
        }

    }


    @Test
    fun test_fetchSpeedReport() {
        var testTime = HashMap<String, Long>()
        var response: Response<GenericApiResponse<SpeedReportResponse>>? = null
        val accessToken = mApiToken
        val bstIds = mApi.fetchVehicleList("1", accessToken, 30)
                .execute()
                .body()
                ?.data?.map {
            it.bstid
        }
        assertTrue(bstIds?.isNotEmpty()!!)
        bstIds.forEach {
            val millis = measureTimeMillis {
                response = mApi.fetchSpeedReport(it, DateTime.now().toString("yyyy-MM-dd"), accessToken).execute()
            }

            assertNotNull(response)
            val data = response?.body()?.data
            assertTrue(response?.isSuccessful!!)
            assertNotNull(data)
            assertTrue(data!!.vehicle?.bstid == it)

            testTime[it] = millis
        }
        testTime.forEach { bstId, time ->
            System.out.println("$bstId -> $time")
        }

    }


    @Test
    fun test_fetchExpenseHeader() {
        var response: Response<GenericApiResponse<List<ExpenseHeader>>>? = null
        val accessToken = mApiToken

        val millis = measureTimeMillis {
            response = mApi.fetchExpenseHeader(accessToken).execute()
        }

        assertNotNull(response)
        val data = response?.body()?.data
        assertTrue(response?.isSuccessful!!)
        assertNotNull(data)
        assertTrue(data!!.isNotEmpty())

        System.out.println("Time: $millis")
    }
*/


//    private fun getApiToken(): String {
//        val response = mApi.postLogin(
//                TEST_USERNAME,
//                "11235813213455",
//                UUID.randomUUID().toString(),
//                "android",
//                "")
//                .execute()
//
//        assertTrue(response.isSuccessful)
//        return response.body()?.accessToken ?: ""
//    }

    @After
    fun tearDown() {

        ///mApi.logout(mApiToken).execute()
    }
}