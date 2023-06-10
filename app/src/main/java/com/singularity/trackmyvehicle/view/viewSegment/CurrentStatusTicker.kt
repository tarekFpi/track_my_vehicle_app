package com.singularity.trackmyvehicle.view.viewSegment

import android.os.Handler

interface CurrentStatusTicker {
    var mCurrentStatusHandler: Handler

    var mCurrentStatusRunnable: Runnable

/* Every 10 sec update current location. */
    fun currentStatusRunnable(): Runnable {
        return object : Runnable {
            var count = 0;
            override fun run() {
                fetchCurrentLocation()
                if (count % 6 == 1) {
                    fetchCurrentTravelledDistance()
                }
                count++
                mCurrentStatusHandler.postDelayed(this, LOCATION_REFRESH_INTERVAL)
                count %= 6
            }
        }
    }

    fun fetchCurrentLocation()
    fun fetchCurrentTravelledDistance()
    fun startTicker() {
        fetchCurrentLocation()
        fetchCurrentTravelledDistance()
        mCurrentStatusHandler.post(mCurrentStatusRunnable)
    }

    fun stopTicker() {
        mCurrentStatusHandler.removeCallbacks(mCurrentStatusRunnable)
    }

    companion object {
        const val LOCATION_REFRESH_INTERVAL: Long = 10000
    }
}