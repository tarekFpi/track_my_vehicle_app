package com.singularity.trackmyvehicle.repository.implementation.v3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.db.dao.NotificationDao
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.NotificationRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.view.fragment.NotificationListFragment
import org.joda.time.DateTime
import javax.inject.Inject


class INotificationRepository @Inject constructor(
        private val mApi: WebService,
        private val mExecutors: AppExecutors,
        private val mDao: NotificationDao,
        private val mNetworkAvailabilityChecker: NetworkAvailabilityChecker,
        private val mPrefRepository: PrefRepository
) : NotificationRepository {
    override fun fetchNotificationList(offset: Int,
                                       query: String): MutableLiveData<Resource<NotificationResponseWrapper>> {
        val mutableLiveData = MutableLiveData<Resource<NotificationResponseWrapper>>()
        mExecutors.networkThread {
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                val notificationResponseWrapper = NotificationResponseWrapper()
                notificationResponseWrapper.data = mDao.getAllNotification("%$query%")
                notificationResponseWrapper.count = notificationResponseWrapper.data?.size ?: 0
                notificationResponseWrapper.firstPage = false
                mutableLiveData.postValue(Resource.error("No network", notificationResponseWrapper))
                return@networkThread
            }
            mutableLiveData.postValue(Resource.loading())
            try {
                val response = mApi.getMessages(ENDPOINTS.MESSAGE_LIST,
                        DateTime.now().minusDays(30).toString("yyyy-MM-dd HH:mm:ss"),
                        NotificationListFragment.PER_PAGE_MESSAGE,
                        offset, query).execute()
                if (response.isSuccessful && response.body()?.isFailed() == false) {
                    val output = response.body()?.response
                    val data = output?.data
                    mDao.save(data)
                    if (offset <= 1) {
                        output?.firstPage = true
                    }
                    mutableLiveData.postValue(Resource.success(output))
                  } else {
                    mutableLiveData.postValue(Resource.error(response.body()?.error?.description
                            ?: "Something went wrong"))
                }
              } catch (ex: Exception) {
                mutableLiveData.postValue(
                        Resource.error(ex.meaningfulError()))
            }
        }
        return mutableLiveData
    }

    override fun fetchUnreadNotificationCount(): LiveData<Resource<Int>> {
        val mutableLiveData = MutableLiveData<Resource<Int>>()
        mExecutors.networkThread {
            if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                mutableLiveData.postValue(Resource.error("No network",mPrefRepository.unreadMessageCount))
                return@networkThread
            }
            mutableLiveData.postValue(Resource.loading(mPrefRepository.unreadMessageCount))
            try {
                val response = mApi.getMessages(ENDPOINTS.MESSAGE_LIST,
                        DateTime.now().minusDays(30).toString("yyyy-MM-dd HH:mm:ss"),
                        1,
                        1,
                        "",
                        isRead = "0"
                ).execute()
                if (response.isSuccessful && response.body()?.isFailed() == false) {
                    val output = response.body()?.response
                    mPrefRepository.saveUnreadMessageCount(output?.count)
                    mutableLiveData.postValue(Resource.success(output?.count))
                } else {
                    mutableLiveData.postValue(Resource.error(response.body()?.error?.description
                            ?: "Something went wrong",mPrefRepository.unreadMessageCount))
                }
            } catch (ex: Exception) {
                mutableLiveData.postValue(
                        Resource.error(ex.meaningfulError(),mPrefRepository.unreadMessageCount))
            }
        }
        return mutableLiveData
    }
}

class NotificationResponseWrapper {
    @SerializedName("Record")
    var data: List<Notification>? = null
    @SerializedName("Count")
    var count: Int? = null
    var firstPage: Boolean = false
}