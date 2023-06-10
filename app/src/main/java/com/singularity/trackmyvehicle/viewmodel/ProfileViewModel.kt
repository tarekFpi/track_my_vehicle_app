package com.singularity.trackmyvehicle.viewmodel

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.AES
import com.singularity.trackmyvehicle.EncryptionConstants
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.MiddleWebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import javax.inject.Inject
import com.singularity.trackmyvehicle.repository.implementation.v2.ProfileRepositoryImpl as ProfileRepositoryV2
import com.singularity.trackmyvehicle.repository.implementation.v3.ProfileRepositoryImpl as ProfileRepositoryV3

/**
 * Created by Sadman Sarar on 3/13/18.
 */

class ProfileViewModel @Inject
constructor(
        private val mV2ProfileRepo: ProfileRepositoryV2,
        private val mV3ProfileRepo: ProfileRepositoryV3,
        private val mUserSource: UserSource,
        private val mPrefRepo: PrefRepository,
        private val mExecutors: AppExecutors,
        private val mMiddleWebService: MiddleWebService,
        private val mNetworkAvailabilityChecker: NetworkAvailabilityChecker
) : ViewModel() {

    fun fetchOrGetProfileInformation(): MutableLiveData<Resource<Profile>> {
        return if (mUserSource == UserSource.VERSION_2) {
            mV2ProfileRepo.fetchOrGetProfileInformation()
        } else {
            mV3ProfileRepo.fetchOrGetProfileInformation()
        }
    }

    fun fetchProfileInformation(): MutableLiveData<Resource<Profile>> {
        return if (mUserSource == UserSource.VERSION_2) {
            mV2ProfileRepo.fetchProfileInformation()
        } else {
            mV3ProfileRepo.fetchProfileInformation()
        }
    }

    fun getProfile(): Profile? {
        return mV2ProfileRepo.getProfile()
    }

    fun requestForgetPasswordOtp(username: String,
                                 mobile: String): MutableLiveData<Resource<OtpResponse>> {
        val liveData = MutableLiveData<Resource<OtpResponse>>()
        liveData.postValue(Resource.loading(null))
        mExecutors.networkThread {
            val version = getUserVersion(username)
            val resource: Resource<OtpResponse> = when (version) {
                "v2" -> if (mobile.isEmpty()) Resource.error<OtpResponse>(
                        "Mobile number is required", null)
                else mV2ProfileRepo.doRequestForgetPasswordApiToNetwork(username, mobile)
                "v3" -> mV3ProfileRepo.doRequestForgetPasswordApiToNetwork(username)
                else -> Resource.error(version ?: "Something went wrong", null)
            }
            liveData.postValue(resource)
        }
        return liveData
    }

    fun validateOtp(otp: String): MutableLiveData<Resource<OtpValidationResponse>> {
        return mV2ProfileRepo.validateOtp(otp)
    }

    fun resetPassword(username: String, passwordToken: String,
                      password: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
        return mV2ProfileRepo.resetPassword(username, passwordToken, password)
    }

    fun changePassword(username: String, currentPassword: String,
                       newPassword: String): MutableLiveData<Resource<GenericApiResponse<String>>> {
        return if (mUserSource == UserSource.VERSION_2) {
            mV2ProfileRepo.changePassword(username, currentPassword, newPassword)
        } else {
            mV3ProfileRepo.changePassword(username, currentPassword, newPassword)
        }
    }

    fun logout(callbackComplete: () -> Unit) {
        when(mUserSource.identifier) {
            UserSource.VERSION_2.identifier -> mV2ProfileRepo.logout(callbackComplete)
            UserSource.VERSION_3.identifier -> mV3ProfileRepo.logout(callbackComplete, true)
            else -> { callbackComplete() }
        }

    }

    private fun getEncryptedUserName(username: String): String {
        return AES.encrypt(username, EncryptionConstants.ENCRYPTION_KEY)
    }

    @WorkerThread
    fun getUserVersion(toString: String): String? {
        try {
            val encryptedUserName = getEncryptedUserName(toString)
            val response = mMiddleWebService.fetchUserVersion(encryptedUserName).execute()
            if (response.isSuccessful) {
                mPrefRepo.saveUserSource(response.body()?.data?.version)
                return response.body()?.data?.version
            }
            if (response.code() == 400) {
                return "Developer error. Bad request."
            }
            if (response.code() == 403) {
                return "Developer error. Forbidden request."
            }
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                return "Network error"
            }
            return "Server Error. Something went wrong."
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                return "Network error"
            }
            return "Server Error. Something went wrong."
        }
    }

    fun resetPasswordV3(username:String,otp: String, token: String,
                        password: String): MutableLiveData<Resource<GenericApiResponse<String>>>? {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        liveData.postValue(Resource.loading(null))
        mExecutors.networkThread {
            liveData.postValue(mV3ProfileRepo.doResetPasswordWithTop(username,otp, password, token))
        }
        return liveData
    }


}