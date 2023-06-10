package com.singularity.trackmyvehicle.viewmodel

import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.di.AppModule
import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.implementation.v2.ProfileRepositoryImpl
import com.singularity.trackmyvehicle.repository.implementation.v3.ProfileRepositoryImpl as ProfileRepositoryV3
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
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
class ProfileViewModelTest: BaseViewModelTest() {
    private lateinit var mProfileViewModel: ProfileViewModel

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
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
            println(it)
        })
        val networkChecker = object :NetworkAvailabilityChecker {
            override fun isNetworkAvailable(): Boolean {
                return true
            }
        }
        val ProfileRepositoryV2 = ProfileRepositoryImpl(mWebService, mPrefRepo, appExecutor)
     //   val profileRepositoryV3 = ProfileRepositoryV3(AppModule().provideWebService(AppModule().provideOkHttpClientV3(interceptor)), mPrefRepo, appExecutor,networkChecker)


        /*mProfileViewModel = ProfileViewModel(ProfileRepositoryV2,profileRepositoryV3,UserSource.VERSION_2,

                appExecutor,)*/
    }

    @Test
    fun test_notNull() {
        assertNotNull(mProfileViewModel)
    }

    @Test
    fun test_fetchProfileInformationSuccessDataFromNetwork() {
        val profile = mProfileFaker.getSingleItem()
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchUserInfo(mPrefRepo.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<Profile>>() {
                    override fun execute(): Response<GenericApiResponse<Profile>> {
                        val response = GenericApiResponse<Profile>()
                        response.data = profile
                        response.code = "200"
                        response.context = "profile"
                        return  Response.success(response)
                    }
                })
        val response = mProfileViewModel.fetchProfileInformation()
        val resource = getBlockingValue(response)

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals(profile, resource.data)
    }

    @Test
    fun test_fetchProfileInformationCausesNetworkExceptionDataFromPrefRepo() {
        val profile = mProfileFaker.getSingleItem()
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepo.profile()).thenReturn(profile)
        `when`(mWebService.fetchUserInfo(mPrefRepo.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<Profile>>() {
                    override fun execute(): Response<GenericApiResponse<Profile>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mProfileViewModel.fetchProfileInformation()
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals(profile, resource.data)

        val profileFromPrefRepo = mProfileViewModel.getProfile()
        assertEquals(profile, profileFromPrefRepo)
    }

    @Test
    fun test_fetchProfileInformationCausesNetworkFailureDataFromPrefRepo() {
        val profile = mProfileFaker.getSingleItem()
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mPrefRepo.profile()).thenReturn(profile)
        `when`(mWebService.fetchUserInfo(mPrefRepo.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<Profile>>() {
                    override fun execute(): Response<GenericApiResponse<Profile>> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })
        val response = mProfileViewModel.fetchProfileInformation()
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals(profile, resource.data)
    }

    @Test
    fun test_requestForgetPasswordOtpFromNetworkSuccess(){
        `when`(mWebService.requestForgetPasswordApi("demo", "0123456789"))
                .thenReturn(object : AppMockCall<OtpResponse>(){
                    override fun execute(): Response<OtpResponse> {
                        val response = OtpResponse()
                        response.otpToken = "otpToken"
                        response.code = 200
                        response.appMessage = "appMessage"
                        return Response.success(response)
                    }
                })

        val otpResponse = mProfileViewModel
                .requestForgetPasswordOtp("demo", "0123456789")
        val otpResource = getBlockingValue(otpResponse)
        val data = otpResource.data

        assertEquals(Status.SUCCESS, otpResource.status)
        assertEquals(200, data?.code)
        assertEquals("appMessage", data?.appMessage)
    }

    @Test
    fun test_requestForgetPasswordOtpFromNetworkThrowsException(){
        `when`(mWebService.requestForgetPasswordApi("demo", "0123456789"))
                .thenReturn(object : AppMockCall<OtpResponse>(){
                    override fun execute(): Response<OtpResponse> {
                        throw SocketTimeoutException()
                    }
                })

        val otpResponse = mProfileViewModel
                .requestForgetPasswordOtp("demo", "0123456789")
        val otpResource = getBlockingValue(otpResponse)

        assertEquals(Status.ERROR, otpResource.status)
        assertEquals("Something went wrong", otpResource.message)
    }

    @Test
    fun test_requestForgetPasswordOtpFromNetworkReturnsError(){
        `when`(mWebService.requestForgetPasswordApi("demo", "0123456789"))
                .thenReturn(object : AppMockCall<OtpResponse>(){
                    override fun execute(): Response<OtpResponse> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })

        val otpResponse = mProfileViewModel
                .requestForgetPasswordOtp("demo", "0123456789")
        val otpResource = getBlockingValue(otpResponse)

        assertEquals(Status.ERROR, otpResource.status)
        assertEquals("Something went wrong", otpResource.message)
    }

    @Test
    fun test_validateOtpUsingNetworkReturnsSuccess() {
        `when`(mPrefRepo.otpToken()).thenReturn("otpToken")
        `when`(mWebService.validateOtp("demo", mPrefRepo.otpToken()))
                .thenReturn(object : AppMockCall<OtpValidationResponse>(){
                    override fun execute(): Response<OtpValidationResponse> {
                        val response = OtpValidationResponse()
                        response.code = "200"
                        response.appMessage = "appMessage"
                        response.passwordToken = "passwordToken"
                        response.context = "validateOtp"
                        return Response.success(response)
                    }
                })
        val response = mProfileViewModel.validateOtp("demo")
        val resource = getBlockingValue(response)
        val data = resource.data

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals("200", data?.code)
        assertEquals("appMessage", data?.appMessage)
        assertEquals("validateOtp", data?.context)
    }

    @Test
    fun test_validateOtpUsingNetworkReturnsError() {
        `when`(mPrefRepo.otpToken()).thenReturn("otpToken")
        `when`(mWebService.validateOtp("demo", mPrefRepo.otpToken()))
                .thenReturn(object : AppMockCall<OtpValidationResponse>(){
                    override fun execute(): Response<OtpValidationResponse> {
                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("text/plain"),
                                "testError"
                        ))
                    }
                })
        val response = mProfileViewModel.validateOtp("demo")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_validateOtpUsingNetworkThrowsException() {
        `when`(mPrefRepo.otpToken()).thenReturn("otpToken")
        `when`(mWebService.validateOtp("demo", mPrefRepo.otpToken()))
                .thenReturn(object : AppMockCall<OtpValidationResponse>(){
                    override fun execute(): Response<OtpValidationResponse> {
                        throw SocketTimeoutException()
                    }
                })

        val response = mProfileViewModel.validateOtp("demo")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_resetPasswordUsingNetworkReturnsSuccess() {
        `when`(mWebService.resetPassword(
                "userName",
                "passwordToken",
                "password")).thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
            override fun execute(): Response<GenericApiResponse<String>> {
                val response = GenericApiResponse<String>()
                response.code = "200"
                response.context = "resetPassword"
                response.userMessage = "userMessage"
                response.data = "passwordReseted"
                return Response.success(response)
            }
        })

        val response = mProfileViewModel
                .resetPassword("userName", "passwordToken", "password")
        val resource = getBlockingValue(response)
        val data = resource.data

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals("200", data?.code)
        assertEquals("resetPassword", data?.context)
        assertEquals("passwordReseted", data?.data)
    }

    @Test
    fun test_resetPasswordUsingNetworkReturnsError() {
        `when`(mWebService.resetPassword(
                "userName",
                "passwordToken",
                "password")).thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
            override fun execute(): Response<GenericApiResponse<String>> {
                return Response.error(403, ResponseBody.create(
                        MediaType.parse("text/plain"),
                        "testError"
                ))
            }
        })

        val response = mProfileViewModel
                .resetPassword("userName", "passwordToken", "password")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_resetPasswordUsingNetworkThrowsException() {
        `when`(mWebService.resetPassword(
                "userName",
                "passwordToken",
                "password")).thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
            override fun execute(): Response<GenericApiResponse<String>> {
                throw SocketTimeoutException()
            }
        })

        val response = mProfileViewModel
                .resetPassword("userName", "passwordToken", "password")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_changePasswordUsingMockNetworkReturnsSuccess(){
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mWebService.logout("apiToken"))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>(){
                    override fun execute(): Response<GenericApiResponse<String>> {
                        val response = GenericApiResponse<String>()
                        response.data = "passwordChanged"
                        response.code = "200"
                        response.userMessage = "passwordChangedSuccessfully"
                        return Response.success(response)
                    }
                })
        `when`(mWebService.changePassword(
                "userName",
                "currentPassword",
                "newPassword",
                "apiToken")).thenReturn(object : AppMockCall<GenericApiResponse<String>>(){
            override fun execute(): Response<GenericApiResponse<String>> {
                val response = GenericApiResponse<String>()
                response.data = "passwordChanged"
                response.code = "200"
                response.userMessage = "passwordChangedSuccessfully"
                return Response.success(response)
            }
        })
        mProfileViewModel.logout{ }
        val response = mProfileViewModel
                .changePassword("userName", "currentPassword", "newPassword")
        val resource = getBlockingValue(response)
        val data = resource.data

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals("200", data?.code)
        assertEquals("passwordChanged", data?.data)
    }

    @Test
    fun test_changePasswordUsingMockNetworkReturnsError(){
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mWebService.changePassword(
                "userName",
                "currentPassword",
                "newPassword",
                "apiToken")).thenReturn(object : AppMockCall<GenericApiResponse<String>>(){
            override fun execute(): Response<GenericApiResponse<String>> {
                return Response.error(404, ResponseBody.create(
                        MediaType.parse("text/plain"),
                        "testError"
                ))
            }
        })
        val response = mProfileViewModel
                .changePassword("userName", "currentPassword", "newPassword")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }

    @Test
    fun test_changePasswordUsingMockNetworkThrowsException(){
        `when`(mPrefRepo.apiToken()).thenReturn("apiToken")
        `when`(mWebService.changePassword(
                "userName",
                "currentPassword",
                "newPassword",
                "apiToken")).thenReturn(object : AppMockCall<GenericApiResponse<String>>(){
            override fun execute(): Response<GenericApiResponse<String>> {
                throw SocketTimeoutException()
            }
        })
        val response = mProfileViewModel
                .changePassword("userName", "currentPassword", "newPassword")
        val resource = getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
        assertEquals("Something went wrong", resource.message)
    }
}