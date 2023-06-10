package com.singularity.trackmyvehicle.utils

import android.os.SystemClock
import androidx.collection.ArrayMap

import java.util.concurrent.TimeUnit

/**
 * Created by Sadman Sarar on 8/3/18.
 * Helper class for managing refresh rate
 * link: https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample
 */
class RateLimiter<KEY>(timeout: Int, timeUnit: TimeUnit) {
    private val timestamps = ArrayMap<KEY, Long>()
    private val timeout: Long = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shouldFetch(key: KEY): Boolean {
        val lastFetched = timestamps[key]
        val now = now()
        if (lastFetched == null) {
            timestamps.put(key, now)
            return true
        }
        if (now - lastFetched > timeout) {
            timestamps.put(key, now)
            return true
        }
        return false
    }

    private fun now(): Long {
        return SystemClock.uptimeMillis()
    }

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }
}
