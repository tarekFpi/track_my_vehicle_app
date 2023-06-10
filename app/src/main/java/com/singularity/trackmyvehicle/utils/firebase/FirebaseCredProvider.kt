package com.singularity.trackmyvehicle.utils.firebase

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId

/**
 * Created by Imran Chowdhury on 8/27/2018.
 */

interface FirebaseCredProvider {
    fun getToken(): String?
}

class FirebaseCredProviderImpl : FirebaseCredProvider {
    override fun getToken(): String? {
        Log.d("kgjk","FirebaseCredProviderImpl")
        return FirebaseInstanceId.getInstance().token
    }
}