package com.singularity.trackmyvehicle.repository.implementation

import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.DistanceReportResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.SpeedReportResponse
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.implementation.v2.ReportsRepositoryImpl
import com.singularity.trackmyvehicle.repository.interfaces.ReportsRepository
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import com.singularity.trackmyvehicle.testhelper.TestHelper
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Created by Imran Chowdhury on 8/13/2018.
 */
@RunWith(MockitoJUnitRunner::class)
class ReportsRepositoryTest : BaseRepositoryTest() {
    private lateinit var mReportRepo: ReportsRepository

    @Before
    fun setUpReportRepo() {
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

      /*  mReportRepo = ReportsRepositoryImpl(
                mReportDao,
                mWebService,
                mPrefRepository,
                appExecutor,
        )*/
    }

    @Test
    fun testNotNullObject() {
        assertNotNull(mReportRepo)
        assertNotNull(mSpeedAlertReportFaker)
        assertNotNull(mVehicleFaker)
    }

    @Test
    fun test_fetchSpeedReportFromNetworkSuccessfully() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchSpeedReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<SpeedReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<SpeedReportResponse>> {
                        val response = GenericApiResponse<SpeedReportResponse>()
                        response.code = "200"
                        response.userMessage = "Success"
                        val model = SpeedReportResponse()
                        val list = mSpeedAlertReportFaker.getList().toList()
                        val vehicle = mVehicleFaker.getSingleItem()
                        model.speedAlert = list
                        model.vehicle = vehicle
                        response.data = model
                        return Response.success(response)
                    }
                })
        val response = mReportRepo.fetchSpeedReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.SUCCESS, resource.status)

        val response2 = mReportRepo.fetchSpeedViolations("BSTID007", TestHelper.getDate())
        val resource2 = getBlockingValue(response2)
        assertEquals(Status.SUCCESS, resource2.status)
    }

    @Test
    fun test_fetchSpeedReportFromNetworkThrowsException() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchSpeedReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<SpeedReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<SpeedReportResponse>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mReportRepo.fetchSpeedReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        val response2 = mReportRepo.fetchSpeedViolations("BSTID007", TestHelper.getDate())
        val resource2 = getBlockingValue(response2)
        assertEquals(Status.ERROR, resource2.status)
    }

    @Test
    fun test_fetchSpeedReportFromNetworkFailure() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchSpeedReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<SpeedReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<SpeedReportResponse>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("This Is An Error"),
                                "Error"
                        ))
                    }
                })
        val response = mReportRepo.fetchSpeedReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)

        val response2 = mReportRepo.fetchSpeedViolations("BSTID007", TestHelper.getDate())
        val resource2 = getBlockingValue(response2)
        assertEquals(Status.ERROR, resource2.status)
    }

    @Test
    fun test_fetchDistanceReportFromNetworkSuccess() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchDistanceReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<DistanceReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<DistanceReportResponse>> {
                        val response = GenericApiResponse<DistanceReportResponse>()
                        val distanceReport = DistanceReportResponse()
                        distanceReport.distance = mDistanceReportFaker.getList().toList()
                        distanceReport.vehicle = mVehicleFaker.getSingleItem()
                        response.data = distanceReport
                        response.code = "200"
                        response.userMessage = "Success"
                        return Response.success(response)
                    }
                })

        val response = mReportRepo.fetchDistanceReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)
        assertEquals(Status.SUCCESS, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchDistanceReportFromNetworkFailureDistanceReportListFromDB() {
        `when`(mReportDao.getDistanceReport("BSTID007", "%${TestHelper.getDate().substring(0, 7)}%")).thenReturn(mDistanceReportFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchDistanceReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<DistanceReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<DistanceReportResponse>> {
                        return Response.error(401, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })

        val response = mReportRepo.fetchDistanceReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchDistanceReportFromNetworkThrowsExceptionDistanceReportListFromDB() {
        `when`(mReportDao.getDistanceReport("BSTID007", "%${TestHelper.getDate().substring(0, 7)}%")).thenReturn(mDistanceReportFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchDistanceReport("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<DistanceReportResponse>>() {
                    override fun execute(): Response<GenericApiResponse<DistanceReportResponse>> {
                        throw SocketTimeoutException()
                    }
                })

        val response = mReportRepo.fetchDistanceReport("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchExpenseHeaderFromNetworkSuccessExpenseHeaderListFromNetwork() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchExpenseHeader(mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<ExpenseHeader>>>() {
                    override fun execute(): Response<GenericApiResponse<List<ExpenseHeader>>> {
                        val response = GenericApiResponse<List<ExpenseHeader>>()
                        response.data = mExpenseHeaderFaker.getList()
                        response.userMessage = "expense header list"
                        response.code = "200"
                        return  Response.success(response)
                    }
                })
        val response = mReportRepo.fetchExpenseHeader()
        val resource = getBlockingValue(response)
        assertEquals(Status.SUCCESS, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchExpenseHeaderFromNetworkFailureExpenseHeaderListFromDb() {
        `when`(mReportDao.expenseHeader).thenReturn(mExpenseHeaderFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchExpenseHeader(mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<ExpenseHeader>>>() {
                    override fun execute(): Response<GenericApiResponse<List<ExpenseHeader>>> {
                        return  Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })
        val response = mReportRepo.fetchExpenseHeader()
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_fetchExpenseHeaderFromNetworkThrowsExceptionExpenseHeaderListFromDb() {
        `when`(mReportDao.expenseHeader).thenReturn(mExpenseHeaderFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchExpenseHeader(mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<ExpenseHeader>>>() {
                    override fun execute(): Response<GenericApiResponse<List<ExpenseHeader>>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mReportRepo.fetchExpenseHeader()
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
        val data = resource.data
        assertEquals(5, data?.size)
    }

    @Test
    fun test_postCreateExpenseFromNetworkReturnsSuccess() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.postExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
                    override fun execute(): Response<GenericApiResponse<String>> {
                        val response = GenericApiResponse<String>()
                        response.code = "200"
                        response.userMessage = "success"
                        response.data = "saved"
                        return Response.success(response)
                    }
                })
        val response = mReportRepo.postCreateExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details")
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.SUCCESS, resource.status)
        assertEquals("saved", data?.data)
        assertEquals("200", data?.code)
    }

    @Test
    fun test_postCreateExpenseFromNetworkThrowsException() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.postExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
                    override fun execute(): Response<GenericApiResponse<String>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mReportRepo.postCreateExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details")
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_postCreateExpenseFromNetworkCausesFailure() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.postExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
                    override fun execute(): Response<GenericApiResponse<String>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })
        val response = mReportRepo.postCreateExpense("BSTID007", TestHelper.getDate(), "expense", 50, "details")
        val resource = getBlockingValue(response)
        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_fetchExpenseFromNetworkSuccessResponseFromNetwork() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchPreviousExpense("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<Expense>>>() {
                    override fun execute(): Response<GenericApiResponse<List<Expense>>> {
                        val response = GenericApiResponse<List<Expense>>()
                        response.code = "200"
                        response.data = mExpenseFaker.getList()
                        return Response.success(response)
                    }
                })

        val response = mReportRepo.fetchExpense("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals(5, resource.data?.size)
    }

    @Test
    fun test_fetchExpenseFromNetworkThrowsExceptionResponseFromDb() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mReportDao.getExpense("BSTID007", "%${TestHelper.getDate().substring(0, 7)}%")).thenReturn(mExpenseFaker.getList())
        `when`(mWebService.fetchPreviousExpense("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<Expense>>>() {
                    override fun execute(): Response<GenericApiResponse<List<Expense>>> {
                        throw SocketTimeoutException()
                    }
                })

        val response = mReportRepo.fetchExpense("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, resource.data?.size)
    }

    @Test
    fun test_fetchExpenseFromNetworkFailureResponseFromDb() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mReportDao.getExpense("BSTID007", "%${TestHelper.getDate().substring(0, 7)}%")).thenReturn(mExpenseFaker.getList())
        `when`(mWebService.fetchPreviousExpense("BSTID007", TestHelper.getDate(), mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<Expense>>>() {
                    override fun execute(): Response<GenericApiResponse<List<Expense>>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })

        val response = mReportRepo.fetchExpense("BSTID007", TestHelper.getDate())
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, resource.data?.size)
    }
}