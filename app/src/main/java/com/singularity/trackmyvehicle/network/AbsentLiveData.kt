package com.singularity.trackmyvehicle.network

import androidx.lifecycle.LiveData
/**
 * Created by Sadman Sarar on 8/3/18.
 * Helper class for transmitting an empty LiveData - Pretty useful!
 */
class AbsentLiveData<T> private constructor() : LiveData<T>() {
    init {
        postValue(null)
    }

    companion object {
        fun <T> create(): LiveData<T> {

            return AbsentLiveData()
        }
    }
}
