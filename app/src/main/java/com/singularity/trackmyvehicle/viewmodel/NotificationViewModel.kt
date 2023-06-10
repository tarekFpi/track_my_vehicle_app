package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.implementation.v3.NotificationResponseWrapper
import com.singularity.trackmyvehicle.repository.interfaces.NotificationRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import javax.inject.Inject

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */


class NotificationViewModel @Inject constructor(
        private val mNotificationRepository: NotificationRepository,
        private val mExecutors: AppExecutors,
        private val mWebService: WebService
) : ViewModel() {

    fun fetchNotificationList(offset: Int = 1,
                              query: String = ""): MutableLiveData<Resource<NotificationResponseWrapper>> {
        return mNotificationRepository.fetchNotificationList(offset, query)
    }

    fun fetchUnreadNotificationCount(): LiveData<Resource<Int>> {
        return mNotificationRepository.fetchUnreadNotificationCount()
    }

    fun markMessagesAsRead(ids: String) {
        mExecutors.ioThread {
            try {
                mWebService.markMessageAsRead(ids).execute()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}