package com.singularity.trackmyvehicle.repository.implementation.v2

import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.dataModel.LoginModel
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.LoginRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.utils.firebase.FirebaseCredProvider
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */

class LoginRepositoryImpl @Inject constructor(
        private val mApi: WebService,
        private val networkAvailability: NetworkAvailabilityChecker,
        private val mPrefRepository: PrefRepository,
        private val mExecutors: AppExecutors,
        private val mFCMRepository: FCMRepository,
        private val mFirebaseCredProvider: FirebaseCredProvider,
        private val mRandomStringGenerator: RandomStringGenerator
) : LoginRepository {
    @WorkerThread
    override fun login(username: String, password: String): Resource<LoginModel> {
        return doLoginUsingNetwork(username, password)
    }

    @WorkerThread
    private fun doLoginUsingNetwork(username: String, password: String): Resource<LoginModel> {
        try {
            val response = mApi.postLogin(
                    username,
                    password,
                    mRandomStringGenerator.getString(),
                    "android",
                    mFirebaseCredProvider.getToken() ?: ""
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val loginResponse = response.body()
                mPrefRepository.saveApiToken(loginResponse?.accessToken)
                mPrefRepository.saveUser(loginResponse?.user)
                mPrefRepository.saveUserSource(UserSource.VERSION_2.identifier)
                val model = LoginModel()
                loginResponse?.let {
                    model.userName = it.user?.name
                    model.userPhone = it.user?.phone
                    model.userEmail = it.user?.email
                }
//                enableFCM()
//                FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
//                    it.result?.token?.let { it1 -> mFCMRepository.postToken(it1) }
//                }
                Resource.success(model)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            if (networkAvailability.isNetworkAvailable()) {
                return Resource.error("No internet", null)
            }
            return Resource.error(parseErrorBody(), null)
        }
    }

}

