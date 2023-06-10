package com.singularity.trackmyvehicle.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.event.NetworkConnectivityEvent
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class NetworkChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checker : NetworkAvailabilityChecker

    init {
        VehicleTrackApplication.appComponent?.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        EventBus.getDefault().post(NetworkConnectivityEvent(checker.isNetworkAvailable()))
    }
}