package com.singularity.trackmyvehicle.viewmodel

import com.singularity.trackmyvehicle.di.AppModule
import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.implementation.v2.LoginRepositoryImpl
import com.singularity.trackmyvehicle.repository.implementation.v3.ILoginRepository
import com.singularity.trackmyvehicle.repository.interfaces.LoginRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import okhttp3.MediaType
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Created by Imran Chowdhury on 8/18/2018.
 */
class LoginViewModelTest : BaseViewModelTest() {
    private lateinit var mLoginViewModel: LoginViewModel
    private lateinit var mLoginRepository: LoginRepository

    @Before
    fun setUp() {
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

       /* mLoginRepository = LoginRepositoryImpl(
                mWebService,
                mNetworkAvailabilityChecker,
                mPrefRepo,
                appExecutor,
                firebaseCredProvider,
                mRandomStringGenerator
        )*/
    }

    @Test
    fun test_notNull() {
        assertNotNull(mLoginViewModel)
    }

    @Test
    fun test_loginUsingMockNetworkSuccess() {
        `when`(mRandomStringGenerator.getString()).thenReturn("deviceId")
        `when`(firebaseCredProvider.getToken()).thenReturn("fcm")
        `when`(mWebService.postLogin(
                "userName",
                "password",
                "deviceId",
                "android",
                "fcm")).thenReturn(object : AppMockCall<LoginResponse>() {
            override fun execute(): Response<LoginResponse> {
                val response = LoginResponse()
                response.code = 200
                response.userMessage = "Login Successful"
                response.context = "appLogin"
                response.accessToken = "awert009231"
                return Response.success(response)
            }
        })

        val response = mLoginViewModel.login("userName", "password")
        val resource = getBlockingValue(response)
        val data = resource.data

        assertEquals(Status.SUCCESS, resource.status)
    }

    @Test
    fun test_loginUsingMockNetworkError() {
        `when`(mRandomStringGenerator.getString()).thenReturn("deviceId")
        `when`(firebaseCredProvider.getToken()).thenReturn("fcm")
        `when`(mWebService.postLogin(
                "userName",
                "password",
                "deviceId",
                "android",
                "fcm")).thenReturn(object : AppMockCall<LoginResponse>() {
            override fun execute(): Response<LoginResponse> {
                return Response.error(404, ResponseBody.create(
                        MediaType.parse("text/plain"),
                        "testError"
                ))
            }
        })

        val response = mLoginViewModel.login("userName", "password")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_loginUsingMockNetworkThrowsException() {
        `when`(mRandomStringGenerator.getString()).thenReturn("deviceId")
        `when`(firebaseCredProvider.getToken()).thenReturn("fcm")
        `when`(mWebService.postLogin(
                "userName",
                "password",
                "deviceId",
                "android",
                "fcm")).thenReturn(object : AppMockCall<LoginResponse>() {
            override fun execute(): Response<LoginResponse> {
                throw SocketTimeoutException()
            }
        })

        val response = mLoginViewModel.login("userName", "password")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }
}