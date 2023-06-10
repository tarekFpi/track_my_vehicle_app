package com.singularity.trackmyvehicle.fcm

import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.GenericResponse
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService as WebServiceV3

/**
 * Created by Sadman Sarar on 3/19/18.
 */
class FCMRepository @Inject constructor(
        private val mApiV2: WebService,
        private val mApiV3: WebServiceV3,
        private val mPrefRepository: PrefRepository) {


    fun postToken(token: String) {

        if (!mPrefRepository.isUserLoggedIn) {
            return
        }

        when(mPrefRepository.userSource){
            UserSource.VERSION_2.identifier -> {
                mApiV2.sendFCMNotification(token, mPrefRepository.apiToken())
                        .enqueue(object : Callback<GenericApiResponse<String>> {
                            override fun onFailure(call: Call<GenericApiResponse<String>>?, t: Throwable?) {
                                mPrefRepository.saveUnsetFCMToken(token)
                            }

                            override fun onResponse(call: Call<GenericApiResponse<String>>?, response: Response<GenericApiResponse<String>>?) {
                                if (response?.isSuccessful == true) {
                                    mPrefRepository.saveUnsetFCMToken("")
                                    mPrefRepository.saveDeviceFCM(token)
                                    return
                                }
                                mPrefRepository.saveUnsetFCMToken(token)
                            }
                        })
            }
            UserSource.VERSION_3.identifier -> {
                mApiV3.sendFCMNotification(ENDPOINTS.SEND_FCM_TOKEN, token)
                        .enqueue(object : Callback<GenericResponse<List<String>>> {
                            override fun onFailure(call: Call<GenericResponse<List<String>>>, t: Throwable) {
                                mPrefRepository.saveUnsetFCMToken(token)
                            }

                            override fun onResponse(call: Call<GenericResponse<List<String>>>,
                                                    response: Response<GenericResponse<List<String>>>) {
                                if (response.isSuccessful && response.body()?.isFailed() == false) {
                                    mPrefRepository.saveUnsetFCMToken("")
                                    mPrefRepository.saveDeviceFCM(token)
                                    return
                                }
                                mPrefRepository.saveUnsetFCMToken(token)
                            }
                        })
            }
        }


    }

    fun shouldSendFCMToken(): Boolean {
        return mPrefRepository.unsentFCMToken().isNotEmpty()
    }


}