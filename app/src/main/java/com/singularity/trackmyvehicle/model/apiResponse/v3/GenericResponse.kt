package com.singularity.trackmyvehicle.model.apiResponse.v3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import org.greenrobot.eventbus.EventBus

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */

data class GenericResponse<T>(
        @Expose
        @SerializedName("Error")
        var error: Error? = null,
        @Expose
        @SerializedName("Response")
        var response: T? = null,
        @Expose
        @SerializedName("User")
        var userId: UserId? = null
) {
    fun isFailed(): Boolean {
        return userId?.userId == null || error?.code != 0
    }

    fun throwEventIfUnAuthenticated() {
        if (userId?.isGuest == true || userId?.userId == null || userId?.userGroupIdentifierHighest?.toLowerCase() == "guest") {
            EventBus.getDefault().post(LogoutEvent(true))
        }
    }
}
