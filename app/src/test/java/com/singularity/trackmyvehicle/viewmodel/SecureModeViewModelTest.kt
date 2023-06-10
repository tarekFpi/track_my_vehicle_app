package com.singularity.trackmyvehicle.viewmodel

import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.SecureModeReponse
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Created by Imran Chowdhury on 9/17/2018.
 */
class SecureModeViewModelTest: BaseViewModelTest() {

    private lateinit var mSecureModeViewModel: SecureModeViewModel

    @Before
    fun testSecureModeViewModelSetUp() {
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

        mSecureModeViewModel = SecureModeViewModel(mWebService, appExecutor, mPrefRepo)
    }

    @Test
    fun test_notNull() {
        assertNotNull(mSecureModeViewModel)
    }

    @Test
    fun test_fetchSecureModeFromNetwork_shouldReturnSuccessMessage() {
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepo.currentVehicle()).thenReturn("bstId_0007")

        `when`(mWebService.secureMode(
                "apiToken",
                "123456",
                "bstId_0007",
                "")
        ).thenReturn(object : AppMockCall<GenericApiResponse<SecureModeReponse>>() {
            override fun execute(): Response<GenericApiResponse<SecureModeReponse>> {
                val apiResponse = GenericApiResponse<SecureModeReponse>()
                val data = SecureModeReponse()
                data.bid = 88
                data.bstid = "bstId_0007"
                data.ignition = 0
                data.secure = 0
                data.msg = "Vehicle is not secured"
                data.vrn = "vrn_007"

                apiResponse.context = "SecureModeContext"
                apiResponse.userMessage = "Read Secure Status Success"
                apiResponse.code = "202"
                apiResponse.data = data
                apiResponse.appMessage = "Success"

                return Response.success(apiResponse)
            }
        })

        val response = mSecureModeViewModel.secureMode("", "123456")
        val resource = getBlockingValue(response)

        val genericApiResponse = resource.data

        assertEquals("SecureModeContext", genericApiResponse!!.context)
        assertEquals("Read Secure Status Success", genericApiResponse!!.userMessage)
        assertEquals("202",genericApiResponse!!.code)
        assertEquals("Success", genericApiResponse!!.appMessage)

        val secureModeResponse = genericApiResponse!!.data

        assertEquals(88, secureModeResponse!!.bid)
        assertEquals("bstId_0007", secureModeResponse!!.bstid)
        assertEquals(0, secureModeResponse!!.ignition)
        assertEquals(0, secureModeResponse!!.secure)
        assertEquals("Vehicle is not secured", secureModeResponse!!.msg)
        assertEquals("vrn_007", secureModeResponse!!.vrn)
    }

    @Test
    fun test_fetchSecureModeFromNetwork_shouldReturnFailureMessage() {
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepo.currentVehicle()).thenReturn("bstId_0007")

        `when`(mWebService.secureMode(
                "apiToken",
                "123456",
                "bstId_0007",
                "")
        ).thenReturn(object : AppMockCall<GenericApiResponse<SecureModeReponse>>() {
            override fun execute(): Response<GenericApiResponse<SecureModeReponse>> {
                return Response.error(404, ResponseBody.create(
                        MediaType.parse("text/plain"),
                        "testError"
                ))
            }
        })

        val response = mSecureModeViewModel.secureMode("", "123456")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_fetchSecureModeFromNetwork_shouldThrowException() {
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepo.currentVehicle()).thenReturn("bstId_0007")

        `when`(mWebService.secureMode(
                "apiToken",
                "123456",
                "bstId_0007",
                "")
        ).thenReturn(object : AppMockCall<GenericApiResponse<SecureModeReponse>>() {
            override fun execute(): Response<GenericApiResponse<SecureModeReponse>> {
                throw SocketTimeoutException()
            }
        })

        val response = mSecureModeViewModel.secureMode("", "123456")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }
}