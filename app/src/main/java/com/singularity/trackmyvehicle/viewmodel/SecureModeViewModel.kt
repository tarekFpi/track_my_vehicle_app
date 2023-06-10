package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.SecureModeReponse
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 8/27/18.
 */
class SecureModeViewModel
@Inject constructor(private val mApi: WebService,
                    private val mExecutors: AppExecutors,
                    private val mPrefRepository: PrefRepository
) : ViewModel() {


    fun secureMode(secure: String = "", password: String = "")
            : LiveData<Resource<GenericApiResponse<SecureModeReponse>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<SecureModeReponse>>>()
        liveData.value = Resource.loading(null)

        mExecutors.networkThread {
            val resource = doFetchSecureModeFromNetwork(secure, password)
            liveData.postValue(resource)
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchSecureModeFromNetwork(
            secure: String,
            password: String
    ): Resource<GenericApiResponse<SecureModeReponse>> {
        try {
            val response = mApi.secureMode(
                    mPrefRepository.apiToken(),
                    password,
                    mPrefRepository.currentVehicle(),
                    secure
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                Resource.success(response.body())
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

}