package com.singularity.trackmyvehicle.repository.implementation

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.PaginatedVehicleWrapper
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleRouteResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.PaginatedVehicleFetcher
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import com.singularity.trackmyvehicle.testhelper.TestHelper
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Created by Imran Chowdhury on 8/14/2018.
 */
@RunWith(MockitoJUnitRunner::class)
class VehicleRepositoryTest : BaseRepositoryTest() {
    private lateinit var mVehicleRepo: VehicleRepository

    @Before
    fun setUpVehicleRepo() {
        val executor = SameThreadExecutorService()
        val appExecutor = object : AppExecutors {
            override fun ioThread(f: () -> Unit) {
                executor.execute(f)
            }
            override fun networkThread(f: () -> Unit) {
                executor.execute(f)
            }
            override fun mainThread(f: () -> Unit) {
                executor.execute(f)
            }
        }
        mVehicleRepo = mAppModule.providVehicleRepositoryV2(
                mVehicleDao,
                mWebService,
                PaginatedVehicleFetcher(
                        mWebService,
                        mPrefRepository
                ),
                mPrefRepository,
                appExecutor
        )
    }

    @Test
    fun testNotNullObject() {
        assertNotNull(mVehicleRepo)
    }

    @Test
    fun test_getVehicleStatusFromNetworkSuccess() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleStatus("BSTID009", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleStatus>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleStatus>> {
                        val response = GenericApiResponse<VehicleStatus>()
                        response.data = mVehicleStatusFaker.getSingleItem()
                        response.code = "200"
                        response.userMessage = "success"
                        return Response.success(response)
                    }
                })

        val response = mVehicleRepo.getVehicleStatus("BSTID009")
        val resource = getBlockingValue(response)
        assertEquals(Status.SUCCESS, resource.status)
    }

    @Test
    fun test_getVehicleStatusFromNetworkCausesFailure() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleStatus("BSTID009", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleStatus>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleStatus>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })

        val response = mVehicleRepo.getVehicleStatus("BSTID009")
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_getVehicleStatusFromNetworkThrowsException() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleStatus("BSTID009", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleStatus>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleStatus>> {
                        throw SocketTimeoutException()
                    }
                })

        val response = mVehicleRepo.getVehicleStatus("BSTID009")
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_fetchVehicleRoutesFromNetworkSuccessResponseFromNetwork(){
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleRoutes("BSTID009", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleRouteResponse>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleRouteResponse>> {
                        val response = GenericApiResponse<VehicleRouteResponse>()
                        val data = VehicleRouteResponse()
                        data.route = mVehicleRouteFaker.getList()
                        data.vehicle = mVehicleFaker.getSingleItem()
                        response.code = "200"
                        response.data = data
                        return Response.success(response)
                    }
                })
        val response = mVehicleRepo.fetchVehicleRoutes("BSTID009", TestHelper.getDate())
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.SUCCESS, resource.status)
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchVehicleRoutesFromNetworkFailureResponseFromDB(){
        `when`(mVehicleDao.getRoutes("BSTID009", "%${TestHelper.getDate()}%"))
                .thenReturn(mVehicleRouteFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleRoutes("BSTID009", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleRouteResponse>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleRouteResponse>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })
        val response = mVehicleRepo.fetchVehicleRoutes("BSTID009", TestHelper.getDate())
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchVehicleRoutesFromNetworkThrowsExceptionResponseFromDB(){
        `when`(mVehicleDao.getRoutes("BSTID009", "%${TestHelper.getDate()}%"))
                .thenReturn(mVehicleRouteFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchVehicleRoutes("BSTID009", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<VehicleRouteResponse>>() {
                    override fun execute(): Response<GenericApiResponse<VehicleRouteResponse>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mVehicleRepo.fetchVehicleRoutes("BSTID009", TestHelper.getDate())
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, data?.size)
    }

    @Test
    fun test_getCurrentVehicleFromDb() {
        val vehicleFromDb = mVehicleFaker.getSingleItem()
        `when`(mPrefRepository.currentVehicle()).thenReturn(vehicleFromDb.bstid)
        `when`(mVehicleDao.getById(vehicleFromDb.bstid)).thenReturn(vehicleFromDb)
        var vehicleById: Terminal? = null
        mVehicleRepo.getCurrentVehicle { vehicle ->
            vehicleById = vehicle
        }
        assertEquals(vehicleFromDb.bstid, vehicleById?.bstid)
    }

    @Test
    fun test_allOtherSimpleMethods() {
        val liveData = MutableLiveData<List<Vehicle>>()
        liveData.value = mVehicleFaker.getList()
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepository.currentVehicle()).thenReturn(mVehicleFaker.getSingleItem().bstid)
        `when`(mWebService.fetchVehicleList("1", mPrefRepository.apiToken(), 30))
                .thenReturn(object : AppMockCall<PaginatedVehicleWrapper>(){
                    override fun enqueue(callback: Callback<PaginatedVehicleWrapper>?) {
                        callback?.onResponse(this, null)
                    }
                })
        `when`(mVehicleDao.getVehicleWithMaxCount(5)).thenReturn(liveData)
        `when`(mVehicleDao.vehicle).thenReturn(liveData)

        val data = mVehicleRepo.getVehicles(5)
        val list = getBlockingValue(data)
        assertEquals(5, list.size)

        val data2 = mVehicleRepo.getVehicles()
        val list2 = getBlockingValue(data2)
        assertEquals(5, list2.size)

        mVehicleRepo.fetch()
    }
}