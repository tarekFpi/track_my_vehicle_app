package com.singularity.trackmyvehicle.repository.implementation.v2

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.data.readUserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import org.greenrobot.eventbus.EventBus

/**
 * Created by Sadman Sarar on 3/11/18.
 */

class PrefRepositoryImpl constructor(private val mContext: Context) : PrefRepository {

    private val APP_SHARED_PREF = Constants.APP_SHARED_PREF
    private val SP_USER = "SP_USER"
    private val SP_USER_NAME = "SP_USER_NAME"
    private val SP_USER_PASSWORD_HASH = "SP_USER_PASSWORD_HASH"
    private val SP_PROFILE = "SP_PROFILE"
    private val SP_ACCESS_TOKEN = "SP_ACCESS_TOKEN"
    private val SP_CURRENT_VEHICLE = "SP_CURRENT_VEHICLE"
    private val SP_CURRENT_VEHICLE_VRN = "SP_CURRENT_VEHICLE_VRN"
    private val SP_OTP_TOKEN = "SP_OTP_TOKEN"
    private val SP_UNSENT_FCM_TOKEN = "SP_UNSENT_FCM_TOKEN"
    private val SP_FCM_TOKEN = "SP_FCM_TOKEN"
    private val SP_COOKIE = "SP_COOKIE"
    private val SP_USER_SOURCE = "SP_USER_SOURCE"
    private val SP_CURRENT_VEHICLE_TERMINAL_ID = "SP_CURRENT_VEHICLE_TERMINAL_ID"
    private val SP_CURRENT_LOCATION = "SP_CURRENT_LOCATION"
    private val SP_UNREAD_NOTIFICATION = "SP_UNREAD_NOTIFICATION"
    private val sp = mContext.getSharedPreferences(APP_SHARED_PREF, Context.MODE_PRIVATE)

    private fun saveStringToSP(data: String, code: String) {
        val e = sp.edit()
        e.putString(code, data)
        e.apply()
    }

    private fun getStringFromSP(code: String): String {
        val sp = mContext.getSharedPreferences(APP_SHARED_PREF, Context.MODE_PRIVATE)
        return sp.getString(code, "") ?: ""
    }

    private fun saveIntToSP(data: Int, code: String) {
        val e = sp.edit()
        e.putInt(code, data)
        e.apply()
    }

    private fun getIntFromSP(code: String): Int {
        val sp = mContext.getSharedPreferences(APP_SHARED_PREF, Context.MODE_PRIVATE)
        return sp.getInt(code, 0)
    }

    override fun saveApiToken(token: String?) {
        saveStringToSP(token ?: "", SP_ACCESS_TOKEN)
    }

    override fun apiToken(): String {
        return getStringFromSP(SP_ACCESS_TOKEN)
    }

    override fun saveUser(user: LoginResponse.User?) {
        if (user == null) {
            saveStringToSP("", SP_USER)
            return
        }
        val gson = Gson()
        val userString = gson.toJson(user)
        saveStringToSP(userString, SP_USER)
    }

    override fun saveCookie(cookie: String?) {
        saveStringToSP(cookie ?: "", SP_COOKIE)
    }

    override fun changeCurrentVehicle(bstId: String?, vrn: String?, terminalId: String?) {
        saveStringToSP(bstId ?: "", SP_CURRENT_VEHICLE)
        saveStringToSP(vrn ?: "", SP_CURRENT_VEHICLE_VRN)
        saveStringToSP(terminalId?: "", SP_CURRENT_VEHICLE_TERMINAL_ID)
        /** this event is the only source for event */
        EventBus.getDefault().post(CurrentVehicleChangeEvent(bstId ?: "", vrn ?: ""))
    }

    override fun changeCurrentVehicle(bstId: String?, vrn: String?, terminalId: String?, location: String?) {
        saveStringToSP(bstId ?: "", SP_CURRENT_VEHICLE)
        saveStringToSP(vrn ?: "", SP_CURRENT_VEHICLE_VRN)
        saveStringToSP(terminalId?: "", SP_CURRENT_VEHICLE_TERMINAL_ID)
        saveStringToSP(location?: "0.0,0.0", SP_CURRENT_LOCATION)

        /** this event is the only source for event */
        EventBus.getDefault().post(CurrentVehicleChangeEvent(bstId ?: "", vrn ?: ""))
    }

    override fun getCookie(): String {
        return getStringFromSP(SP_COOKIE)
    }

    override fun getUserSource(): String {
        return getStringFromSP(SP_USER_SOURCE)
    }

    override fun currentVehicle(): String {
        return getStringFromSP(SP_CURRENT_VEHICLE)
    }

    override fun currentVehicleVrn(): String {
        return getStringFromSP(SP_CURRENT_VEHICLE_VRN)
    }

    override fun currentLocation(): String {
        return getStringFromSP(SP_CURRENT_LOCATION)
    }

    override fun saveProfile(profile: Profile?) {
        val gson = Gson()
        val profileString = gson.toJson(profile)
        saveStringToSP(profileString, SP_PROFILE)
    }

    override fun profile(): Profile? {
        try {
            val profileString = getStringFromSP(SP_PROFILE)
            val gson = Gson()
            val profile = gson.fromJson(profileString, Profile::class.java)
            return profile
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            return null
        }
    }

    override fun saveOtpToken(token: String?) {
        saveStringToSP(token ?: "", SP_OTP_TOKEN)
    }

    override fun otpToken(): String {
        return getStringFromSP(SP_OTP_TOKEN)
    }
    override fun savePasswordHash(passwordHash: String?) {
        saveStringToSP(passwordHash ?: "", SP_USER_PASSWORD_HASH)
    }

    override fun passwordHash(): String {
        return getStringFromSP(SP_USER_PASSWORD_HASH)
    }

    override fun saveUserName(userName: String?) {
        saveStringToSP(userName ?: "", SP_USER_NAME)
    }

    override fun userName(): String {
        return getStringFromSP(SP_USER_NAME)
    }

    override fun saveUnsetFCMToken(token: String) {
        saveStringToSP(token, SP_UNSENT_FCM_TOKEN)
    }

    override fun unsentFCMToken(): String {
        return getStringFromSP(SP_UNSENT_FCM_TOKEN)
    }

    override fun saveUserSource(source: String) {
        saveStringToSP(source, SP_USER_SOURCE)
    }

    override fun isUserLoggedIn(): Boolean {
        return when (readUserSource(sp)) {
            UserSource.VERSION_2 -> apiToken().isNotEmpty()
            UserSource.VERSION_3 -> cookie.isNotEmpty()
            else -> false
        }
    }

    override fun getCurrentVehicleTerminalId(): String {
        return getStringFromSP(SP_CURRENT_VEHICLE_TERMINAL_ID)
    }

    override fun getUser(): LoginResponse.User? {
        try {
            val userString = getStringFromSP(SP_USER)
            val gson = Gson()
            val user = gson.fromJson(userString, LoginResponse.User::class.java)
            return user
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            return null
        }
    }

    override fun saveUnreadMessageCount(count: Int?) {
        saveIntToSP(count ?: 0,SP_UNREAD_NOTIFICATION)
    }

    override fun getUnreadMessageCount(): Int {
        return getIntFromSP(SP_UNREAD_NOTIFICATION)
    }

    override fun saveDeviceFCM(token: String) {
        saveStringToSP(token, SP_FCM_TOKEN)
    }

    override fun getDeviceFcm(): String {
        return getStringFromSP(SP_FCM_TOKEN)
    }
}