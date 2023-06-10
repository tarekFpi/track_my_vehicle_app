package com.singularity.trackmyvehicle.repository.implementation.v3

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonSyntaxException
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.logoutThread
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.ProfileRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.utils.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Created by Imran Chowdhury on 2020-01-26.
 */

class ProfileRepositoryImpl @Inject
constructor(
        private val mApi: WebService,
        private val mPrefRepository: PrefRepository,
        private val mExecutors: AppExecutors,
        private val mNetworkAvailabilityChecker: NetworkAvailabilityChecker
) : ProfileRepository {
    override fun fetchOrGetProfileInformation(): MutableLiveData<Resource<Profile>> {
        val mutableLiveData = MutableLiveData<Resource<Profile>>()
        mutableLiveData.postValue(Resource.loading(getProfile()))
        mExecutors.ioThread {
            mutableLiveData.postValue(doFetchUserInfoFromBackground())
        }
        return mutableLiveData
    }

    override fun fetchProfileInformation(): MutableLiveData<Resource<Profile>> {
        val liveData = MutableLiveData<Resource<Profile>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(mPrefRepository.profile()))
            mExecutors.networkThread {
                val resource = doFetchUserInfoFromBackground()
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    /*@WorkerThread
    private fun doFetchUserInfoFromBackground(): Resource<Profile> {
        try {
            val response = mApi.getUserProfile(
                    ENDPOINTS.PROFILE_SCRIPT
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val model = Profile()
                response.body()?.let {
                    model.email = it.response?.profile?.userEmail
                    model.name = it.response?.profile?.userSignInName
                    model.mobile = it.response?.profile?.userPhone
                    mExecutors.mainThread {
                        mPrefRepository.saveProfile(model)
                    }
                    it.throwEventIfUnAuthenticated()
                }
                Resource.success(model)
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, mPrefRepository.profile())
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), mPrefRepository.profile())
        }
    }*/

    @WorkerThread
    private fun doFetchUserInfoFromBackground(): Resource<Profile> {

        try {
            val response = mApi.getUserProfileInfo(
                    ENDPOINTS.PROFILE_INFO_SCRIPT,
                    ENDPOINTS.PROFILE_INFO_SUBJECT,
                    ENDPOINTS.PROFILE_INFO_ACTION
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val model = Profile()
                response.body()?.let {
                    model.email = it.profileResponse?.email
                    model.name = it.profileResponse?.nameFirst + " " + it.profileResponse?.nameMiddle + " " + it.profileResponse?.nameLast
                    model.mobile = it.profileResponse?.mobileNumber
                    mExecutors.mainThread {
                        mPrefRepository.saveProfile(model)
                    }
                    //it.throwEventIfUnAuthenticated()
                }
                Resource.success(model)
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, mPrefRepository.profile())
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), mPrefRepository.profile())
        }
    }

    override fun getProfile(): Profile? {
        return mPrefRepository.profile()
    }

    override fun requestForgetPasswordOtp(username: String,
                                          mobile: String): MutableLiveData<Resource<OtpResponse>> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MutableLiveData()
    }

    override fun validateOtp(otp: String): MutableLiveData<Resource<OtpValidationResponse>> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MutableLiveData()
    }

    override fun resetPassword(username: String, passwordToken: String,
                               password: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MutableLiveData()
    }

    override fun changePassword(username: String, currentPassword: String,
                                newPassword: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        liveData.postValue(Resource.loading(null))
        mExecutors.networkThread {
            try {
                val response = mApi.changePassword(
                        ENDPOINTS.PASSWORD_CHANGE,
                        newPassword,
                        newPassword
                ).execute()
                val body = response.body()
                body?.throwEventIfUnAuthenticated()
                if (response.isSuccessful && body?.isFailed() == false) {
                    val data = GenericApiResponse<String>()
                    data.userMessage = null
                    liveData.postValue(Resource.success(data))
                } else if (response.isSuccessful && body?.error?.code != 0) {
                    liveData.postValue(
                            Resource.error(body?.error?.description ?: "Something went wrong",
                                    null))
                } else {
                    liveData.postValue(Resource.error("Something went wrong", null))
                }
            } catch (ex: Exception) {
                liveData.postValue(Resource.error("Something went wrong", null))
            }
        }
        return liveData
    }

    override fun logout(callbackComplete: () -> Unit, dontCare: Boolean) {
        val cookie = mPrefRepository.cookie
        Log.d("cookie","inside this"+cookie)
        disableFCM()

        mPrefRepository.deviceFcm.let {
            logoutThread {
                try {
                    if (dontCare) {
                        mExecutors.mainThread { callbackComplete() }
                    }
                    val client = OkHttpClient.Builder()
                            .readTimeout(2, TimeUnit.MINUTES)
                            .writeTimeout(2, TimeUnit.MINUTES)
                            .callTimeout(2, TimeUnit.MINUTES)
                            .build()
                    val formBody: RequestBody = FormBody.Builder()
                            .add("_Script", ENDPOINTS.LOGOUT)
                            .add("FCMIdentifier", it)
                            .build()
                    val request: Request = Request.Builder()
                            .url(Constants.BASE_URL_V3)
                            .addHeader("Cookie", cookie)
                            .post(formBody)
                            .build()
                    client.newCall(request).execute()
                    if (dontCare) return@logoutThread
                    mExecutors.mainThread { callbackComplete() }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    mExecutors.mainThread { callbackComplete() }
                }
            }
        }

        /*FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.exception != null) {
                callbackComplete()
                return@addOnCompleteListener
            }
            it.result?.token?.let { token ->
                logoutThread {
                    try {
                        if (dontCare) {
                            mExecutors.mainThread { callbackComplete() }
                        }
                        val client = OkHttpClient.Builder()
                                .readTimeout(2, TimeUnit.MINUTES)
                                .writeTimeout(2, TimeUnit.MINUTES)
                                .callTimeout(2, TimeUnit.MINUTES)
                                .build()
                        val formBody: RequestBody = FormBody.Builder()
                                .add("_Script", ENDPOINTS.LOGOUT)
                                .add("FCMIdentifier", token)
                                .build()
                        val request: Request = Request.Builder()
                                .url(Constants.BASE_URL_V3)
                                .addHeader("Cookie", cookie)
                                .post(formBody)
                                .build()
                        client.newCall(request).execute()
                        if (dontCare) return@logoutThread
                        mExecutors.mainThread { callbackComplete() }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        mExecutors.mainThread { callbackComplete() }
                    }
                }
            }
        }*/
    }

    @WorkerThread
    fun doRequestForgetPasswordApiToNetwork(username: String): Resource<OtpResponse> {
        val data = OtpResponse()
        data.context = "otp-v3"

        try {
            val response = mApi.requestOtpPassword(ENDPOINTS.REQUEST_OTP, username, username)
                    .execute()
            val body = response.body()
            return if (response.isSuccessful && body?.isFailed() == false) {
                data.otpToken = body.response?.otp?.token
                Resource.success(data)
            } else {
                Resource.error(body?.error?.description ?: "Error", null)
            }
        } catch (ex: JsonSyntaxException) {
            return Resource.error(
                    "Malformed response returned from Server. Contact support and let us know.",
                    null)
        } catch (ex: Exception) {
            return if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
                Resource.error("Serer Error", null)
            } else {
                Resource.error("Network Error", null)
            }

        }
    }

    @WorkerThread
    fun doResetPasswordWithTop(
            username: String,
            otp: String, password: String,
            token: String): Resource<GenericApiResponse<String>> {
        val data = GenericApiResponse<String>()
        data.context = "otp-v3"

        try {
            val response = mApi.resetPassword(ENDPOINTS.RESET_PASSWORD, otp, password, username,
                    username, token)
                    .execute()
            val body = response.body()
            return if (response.isSuccessful && body?.isFailed() == false) {
                //TODO: make response
                Resource.success(data)
            } else {
                Resource.error(body?.error?.description ?: "Error", null)
            }
        } catch (ex: Exception) {
            return if (mNetworkAvailabilityChecker.isNetworkAvailable()) {
                Resource.error("Serer Error ", null)
            } else {
                Resource.error("Network Error", null)
            }

        }
    }

}