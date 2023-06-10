package com.singularity.trackmyvehicle.utils

/**
 * Created by Imran Chowdhury on 8/18/2018.
 */
interface NetworkAvailabilityChecker {
    /**
     * should return true if network is available
     */
    fun isNetworkAvailable(): Boolean
}