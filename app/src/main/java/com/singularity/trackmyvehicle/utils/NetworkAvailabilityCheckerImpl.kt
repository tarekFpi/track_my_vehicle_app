package com.singularity.trackmyvehicle.utils

import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Inject


/**
 * Created by Sadman Sarar on 3/11/18.
 */

class NetworkAvailabilityCheckerImpl @Inject
constructor(
        private val mContext: Context
): NetworkAvailabilityChecker{

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}