package com.singularity.trackmyvehicle.utils

/**
 * Created by Sadman Sarar on 3/11/18.
 */

interface AppCallback<T> {
    fun callback(t: T)
}

interface AppCallbacks<T, Y> {
    fun callback(t: T, y: Y)
}