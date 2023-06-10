package com.singularity.trackmyvehicle.repository.implementation.v2

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.ProfileRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-26.
 */

class ProfileRepositoryImpl @Inject
constructor(
        private val mApi: WebService,
        private val mPrefRepository: PrefRepository,
        private val mExecutors: AppExecutors
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

    @WorkerThread
    private fun doFetchUserInfoFromBackground(): Resource<Profile> {
        try {
            val response = mApi.fetchUserInfo(
                    mPrefRepository.apiToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                mPrefRepository.saveProfile(response.body()?.data)
                Resource.success(response.body()?.data)
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
        val liveData = MutableLiveData<Resource<OtpResponse>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(null))
            mExecutors.networkThread {
                val resource = doRequestForgetPasswordApiToNetwork(username, mobile)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    fun doRequestForgetPasswordApiToNetwork(username: String,
                                            mobile: String): Resource<OtpResponse> {
        try {
            val response = mApi.requestForgetPasswordApi(
                    username,
                    mobile
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                mPrefRepository.saveOtpToken(response.body()?.otpToken)
                Resource.success(response.body())
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

    override fun validateOtp(otp: String): MutableLiveData<Resource<OtpValidationResponse>> {
        val liveData = MutableLiveData<Resource<OtpValidationResponse>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(null))
            mExecutors.networkThread {
                val resource = doValidateOtpFromNetwork(otp)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doValidateOtpFromNetwork(otp: String): Resource<OtpValidationResponse> {
        try {
            val response = mApi.validateOtp(
                    otp,
                    getExistingOtpToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                Resource.success(response.body())
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

    private fun getExistingOtpToken(): String {
        return mPrefRepository.otpToken()
    }

    override fun resetPassword(username: String, passwordToken: String,
                               password: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(null))
            mExecutors.networkThread {
                val resource = doResetPasswardUsingNetwork(username, passwordToken, password)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doResetPasswardUsingNetwork(username: String, passwordToken: String,
                                            password: String): Resource<GenericApiResponse<String>>? {
        try {
            val response = mApi.resetPassword(
                    username,
                    passwordToken,
                    password
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                Resource.success(response.body())
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

    override fun changePassword(username: String, currentPassword: String,
                                newPassword: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(null))
            mExecutors.networkThread {
                val resource = doChangePasswordUsingNetwork(username, currentPassword, newPassword)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doChangePasswordUsingNetwork(
            username: String,
            currentPassword: String,
            newPassword: String): Resource<GenericApiResponse<String>> {
        try {
            val response = mApi.changePassword(
                    username,
                    currentPassword,
                    newPassword,
                    mPrefRepository.apiToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                Resource.success(response.body())
            } else {
                val userMsg = parseErrorBody(response)
                Resource.error(userMsg, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

    override fun logout(callbackComplete: () -> Unit, dontCare: Boolean) {
        mExecutors.networkThread {
            try {
                mApi.logout(mPrefRepository.apiToken()).execute()
                mExecutors.mainThread {callbackComplete()}
            } catch (ex: Exception) {
                FirebaseCrashlytics.getInstance().recordException(ex)
                ex.printStackTrace()
                mExecutors.mainThread {callbackComplete()}
            }
        }
    }

}