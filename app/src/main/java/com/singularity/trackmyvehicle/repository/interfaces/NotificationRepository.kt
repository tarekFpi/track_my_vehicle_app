package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.implementation.v3.NotificationResponseWrapper
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */

interface NotificationRepository {
    fun fetchNotificationList(offset: Int, query: String): MutableLiveData<Resource<NotificationResponseWrapper>>
    fun fetchUnreadNotificationCount(): LiveData<Resource<Int>>
}
