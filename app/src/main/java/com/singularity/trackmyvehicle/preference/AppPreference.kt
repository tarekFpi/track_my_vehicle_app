package com.singularity.trackmyvehicle.preference

import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse

/**
 * Created by Kariba Yasmin on 7/14/21.
 */
interface AppPreference {

    companion object{
        const val UserName = "username"
        const val phone = "phone"
        const val bstId = "bstId"
        const val SignInName = "sign_in_name"
        const val Password = "password"
        const val isRememberMe = "is_remember_me"
        const val AnalyticsTerminalList = "analytics_terminal_list"
        const val AnalyticsSafeDrivingList = "analytics_safeDriving_list"
        const val isNotificationEnable = "is_notification_enable"
    }

    fun getBstId(bstId: String): String?
    fun SetBstId(key: String,bstId: String)
     fun SetuserName(key: String,UserName: String)
     fun getUserName(UserName: String):String?
     fun Setphone(key: String,phone: String)
     fun getphone(phone: String):String?
    fun getString(key: String): String?
    fun setString(key: String, value: String)
    fun getInt(key: String): Int?
    fun setInt(key : String, value: Int)
    fun getBoolean(key: String): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun getAnalyticsTerminalList(key: String): List<AnalyticsResponse.AnalyticsTerminal>
    fun setAnalyticsTerminalList(key: String, value: List<AnalyticsResponse.AnalyticsTerminal>)
    fun getAnalyticsSafeDrivingList(key: String): List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>
    fun setAnalyticsSafeDrivingList(key: String, value: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>)
}