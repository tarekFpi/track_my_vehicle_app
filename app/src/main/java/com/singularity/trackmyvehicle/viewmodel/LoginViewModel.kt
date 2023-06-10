package com.singularity.trackmyvehicle.viewmodel

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.AES
import com.singularity.trackmyvehicle.EncryptionConstants
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.dataModel.LoginModel
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.MiddleWebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import javax.inject.Inject
import com.singularity.trackmyvehicle.repository.implementation.v2.LoginRepositoryImpl as LoginRepositoryV2
import com.singularity.trackmyvehicle.repository.implementation.v3.ILoginRepository as LoginRepositoryV3

/**
 * Created by Sadman Sarar on 3/11/18.
 */
class LoginViewModel
@Inject constructor(
        private val mV2Repo: LoginRepositoryV2,
        private val mV3Repo: LoginRepositoryV3,
        private val mPrefRepo: PrefRepository,
        private val mExecutors: AppExecutors,
        private val mMiddleWebService: MiddleWebService,
        private val mNetworkAvailabilityChecker: NetworkAvailabilityChecker
) : ViewModel() {

    fun login(username: String, password: String): LiveData<Resource<LoginModel>> {
        val livedata = MutableLiveData<Resource<LoginModel>>()
        livedata.postValue(Resource.loading(null))
        mExecutors.ioThread {
            val userVersion = getUserVersion(username)
            Log.d("userVersion:","${userVersion}")

            val output = when(userVersion) {
                "v2" -> mV2Repo.login(username, password)
                "v3" -> mV3Repo.login(username, password)
                else -> Resource.error<LoginModel>(userVersion ?: "Middleware Failed", null)
            }
            livedata.postValue(output)
        }
        return livedata
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
            if(response.code() == 400) {
                return "Developer error. Bad request."
            }
            if(response.code() == 403) {
                return "Developer error. Forbidden request."
            }
            if(!mNetworkAvailabilityChecker.isNetworkAvailable()){
                return "Network error"
            }
            return "Server Error. Something went wrong."
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            if(!mNetworkAvailabilityChecker.isNetworkAvailable()){
                return "Network error"
            }
            return "Server Error. Something went wrong.${ex.toString()} "
        }
    }
}