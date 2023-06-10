package com.singularity.trackmyvehicle.network.apiExt

/**
 * Created by Sadman Sarar on 2019-04-10.
 */
interface Executable<SyncReturn, AsyncReturn> {

    fun executeAsync(): AsyncReturn
    fun execute(): SyncReturn

}
