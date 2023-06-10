package com.singularity.trackmyvehicle.model.event

import org.joda.time.DateTime

/**
 * Created by Sadman Sarar on 3/12/18.
 */

class CurrentVehicleChangeEvent(var bstId: String, var vrn: String) {

}

class CurrentDateChangeEvent(var date: DateTime) {

}

class LogoutEvent(val isAutomatic : Boolean = false, val fromUrl : String = "")

class NetworkConnectivityEvent(val connected: Boolean)