package com.singularity.trackmyvehicle.preference

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularity.trackmyvehicle.model.apiResponse.v3.AnalyticsResponse
import java.lang.reflect.Type


/**
 * Created by Kariba Yasmin on 7/14/21.
 */
class AppPreferenceImpl(context: Context): AppPreference {

    private val sharedPreferences = context.getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    override fun getBstId(bstId: String): String? {
        return sharedPreferences.getString(bstId, "")
    }

    override fun SetBstId(key: String, bstId: String){
        editor.putString(key, bstId)
        editor.apply()
    }

    override fun SetuserName(key: String, UserName: String) {
        editor.putString(key, UserName)
        editor.apply()
    }

    override fun getUserName(UserName: String): String? {
        return sharedPreferences.getString(UserName, "")
    }

    override fun Setphone(key: String, phone: String) {
        editor.putString(key, phone)
        editor.apply()
    }

    override fun getphone(phone: String): String? {
        return sharedPreferences.getString(phone, "")
    }


    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    override fun setString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    override fun getInt(key: String): Int? {
        return  sharedPreferences.getInt(key, - 1)
    }

    override fun setInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    override fun getAnalyticsTerminalList(key: String): List<AnalyticsResponse.AnalyticsTerminal> {
        var terminalListItems: List<AnalyticsResponse.AnalyticsTerminal> = ArrayList()
        val serializedTerminalListObject = sharedPreferences.getString(key, "")
        if (serializedTerminalListObject != null) {
            val terminalType: Type = object : TypeToken<List<AnalyticsResponse.AnalyticsTerminal>>() {}.type
            terminalListItems = Gson().fromJson<List<AnalyticsResponse.AnalyticsTerminal>>(serializedTerminalListObject, terminalType)
        }

       return terminalListItems
    }

    override fun setAnalyticsTerminalList(key: String, value: List<AnalyticsResponse.AnalyticsTerminal>) {
        val json = Gson().toJson(value)

        setString(key, json)
    }

    override fun getAnalyticsSafeDrivingList(key: String): List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank> {
        var safeDrivingItems: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank> = ArrayList()
        val serializedSafeDrivingListObject = sharedPreferences.getString(key, "")
        if (serializedSafeDrivingListObject != null) {
            val safeDrivingType: Type = object : TypeToken<List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>>() {}.type
            safeDrivingItems = Gson().fromJson<List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>>(serializedSafeDrivingListObject, safeDrivingType)
        }

        return safeDrivingItems
    }

    override fun setAnalyticsSafeDrivingList(key: String, value: List<AnalyticsResponse.AnalyticsDriveSafetyRank.Rank>) {
        val json = Gson().toJson(value)

        setString(key, json)
    }

}