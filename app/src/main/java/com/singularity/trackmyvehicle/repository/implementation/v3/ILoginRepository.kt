package com.singularity.trackmyvehicle.repository.implementation.v3

import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.dataModel.LoginModel
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.LoginRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.toMD5
import com.singularity.trackmyvehicle.utils.*
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-23.
 */


class ILoginRepository @Inject constructor(
    private val mApi: WebService,
    private val networkAvailability: NetworkAvailabilityChecker,
    private val mPrefRepository: PrefRepository,
    private val mFCMRepository: FCMRepository,
    private val mExecutors: AppExecutors
) : LoginRepository {

    @WorkerThread
    override fun login(username: String, password: String): Resource<LoginModel> {
        return doLoginUsingNetwork(username, password)
    }

    @WorkerThread
    private fun doLoginUsingNetwork(username: String, password: String): Resource<LoginModel> {
        try {
            val response = mApi.postLogin(
                ENDPOINTS.LOGIN_SCRIPT,
                username,
                username,
                password
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            if (response.isSuccessful && response.code() == 200) {

                val userResponse = response.body()
                if (userResponse?.error?.code == 0) {
                    mPrefRepository.saveCookie(
                        response.headers().get("Set-Cookie")?.split(";")?.firstOrNull()
                            ?: ""
                    )
                    val cookie = mPrefRepository.cookie
                    Log.d("cookie",
                        "inside this " + response.headers().get("Set-Cookie")?.split(";")
                            ?.firstOrNull()
                    )

                    var userPasswordHashSalt = userResponse?.response?.login?.userPasswordHashSalt

                    var hashPasswordSave = password + userPasswordHashSalt

                    mPrefRepository.savePasswordHash(hashPasswordSave.toMD5())
                    mPrefRepository.saveUserName(username)
                    mPrefRepository.changeCurrentVehicle("", "", "")
                    mPrefRepository.changeCurrentVehicle("", "", "", "")

                    val model = LoginModel()
                    userResponse.response?.login?.let {
                        model.userEmail = it.userEmail
                        model.userGroupIdentifier = it.userGroupIdentifier
                        model.userGroupName = it.userGroupName
                        model.userGroupWeight = it.userGroupWeight
                        model.userId = it.userId
                        model.userName = it.userName
                        model.userPhone = it.userPhone
                        model.userSignInName = it.userSignInName

                        val user = LoginResponse.User()
                        user.email = it.userEmail
                        user.phone = it.userPhone
                        user.name = it.userName
                        user.id = it.userEmail
                        user.userGroupIdentifier = it.userGroupIdentifier
                        mPrefRepository.saveUser(user)
                    }
//                    enableFCM()
//                    FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
//                        it.result?.token?.let { it1 -> mFCMRepository.postToken(it1) }
//                    }
                    return Resource.success(model)
                } else if (userResponse?.error?.code != 0 && userResponse?.error?.description != null) {
                    val userMessage = userResponse.error?.description ?: "Something went wrong"
                    return Resource.error(userMessage, null)



                } else {
                    val userMessage = parseErrorBody(response)
                    return Resource.error(userMessage, null)
                }
            } else {
                val userMessage = parseErrorBody(response)
                return Resource.error(userMessage, null)
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

    @WorkerThread
    fun doLoginUsingNetworkWithPasswordHash(
        username: String,
        passwordHash: String
    ): Resource<LoginModel> {
        Log.d("jvjh", "call login using network Password Hash")

        try {
            val response = mApi.postLoginWithHash(
                ENDPOINTS.LOGIN_SCRIPT,
                username,
                username,
                passwordHash
            ).execute()
            if (response.isSuccessful && response.code() == 200) {

                val userResponse = response.body()
                if (userResponse?.error?.code == 0) {

                    val cookie = response.headers().get("Set-Cookie")?.split(";")?.firstOrNull()

                    Log.d("TestCookie", cookie.toString())
                    mPrefRepository.saveCookie(

                        cookie ?: ""
                    )
                    val model = LoginModel()


                    userResponse.response?.login?.let {
                        model.userEmail = it.userEmail
                        model.userGroupIdentifier = it.userGroupIdentifier
                        model.userGroupName = it.userGroupName
                        model.userGroupWeight = it.userGroupWeight
                        model.userId = it.userId
                        model.userName = it.userName
                        model.userPhone = it.userPhone
                        model.userSignInName = it.userSignInName
                    }
                    return Resource.success(model)
                } else if (userResponse?.error?.code != 0 && userResponse?.error?.description != null) {
                    val userMessage = userResponse.error?.description ?: "Something went wrong"
                    return Resource.error(userMessage, null)
                } else {
                    val userMessage = parseErrorBody(response)
                    return Resource.error(userMessage, null)
                }
            } else {
                return Resource.success(null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            if (!networkAvailability.isNetworkAvailable()) {
                return Resource.success(null)
            }
            return Resource.error("Error")
        }
    }

}