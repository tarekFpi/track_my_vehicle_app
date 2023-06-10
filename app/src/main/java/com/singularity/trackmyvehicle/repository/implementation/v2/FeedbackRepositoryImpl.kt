package com.singularity.trackmyvehicle.repository.implementation.v2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.db.dao.FeedbackDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.PaginatedCallback
import com.singularity.trackmyvehicle.network.PaginatedFeedbackFetcher
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.FeedbackRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/14/18.
 */
class FeedbackRepositoryImpl
@Inject constructor(private val mDao: FeedbackDao,
                    private val mApi: WebService,
                    private val mPrefRepository: PrefRepository,
                    private val mPaginatedFeedbackFetcher: PaginatedFeedbackFetcher,
                    private val mExecutors: AppExecutors
) : FeedbackRepository, PaginatedCallback<Feedback> {

    init {
        mPaginatedFeedbackFetcher.callback = this
    }

    override fun createFeedback(bstId: String, feedbackHeader: String, otherDetails: String)
            : LiveData<Resource<GenericApiResponse<String>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        liveData.postValue(Resource.loading(null))
        mExecutors.networkThread {
            val resource = doCreateFeedbackFromNetworkRequest(bstId, feedbackHeader, otherDetails)
            liveData.postValue(resource)
        }
        return liveData
    }

    override fun fetchExpenseHeader(): MutableLiveData<Resource<List<FeedbackHeader>>> {
        val liveData = MutableLiveData<Resource<List<FeedbackHeader>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getFeedbackHeader()))
            mExecutors.networkThread {
                val resource = doFetchFeedbackHeaderNetworkRequest()
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    override fun fetchFeedback(): LiveData<Resource<List<Feedback>>> {
        val liveData = MutableLiveData<Resource<List<Feedback>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getFeedback()))
            mExecutors.networkThread {
                val resource = doFetchFeedbackFromNetwork()
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    override fun getFeedbacksAsync(): LiveData<MutableList<Feedback>>? {
        return mDao.feedbackAllAsync
    }

    override fun fetchPaginatedFeedback() {
        if (!mPaginatedFeedbackFetcher.isRunning) {
            mPaginatedFeedbackFetcher.fetch()
        }
    }

    override fun fetchEnded(data: List<Feedback>) {
        mExecutors.ioThread {
            mDao.saveFeedback(data)
        }
    }

    override fun fetchFailed(msg: String) {
    }


    override fun fetchFeedbackRemarks(feedbackId: String): LiveData<Resource<List<FeedbackRemark>>> {
        val liveData = MutableLiveData<Resource<List<FeedbackRemark>>>()

        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getFeedbackRemarks(feedbackId)))
            mExecutors.networkThread {
                val resource = doFetchFeedbackRemarksFromNetwork(feedbackId)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchFeedbackRemarksFromNetwork(feedbackId: String): Resource<List<FeedbackRemark>> {
        try {
            val response = mApi.fetchFeedbackRemarks(
                    feedbackId,
                    mPrefRepository.apiToken()
            ).execute()

            return if (response.isSuccessful) {
                val result = response.body()
                val feedbackRemarkList = result?.data
                feedbackRemarkList?.forEach {remark->
                    remark.remarksId = feedbackId
                }
                saveFeedbackRemarkList(feedbackRemarkList)
                Resource.success(feedbackRemarkList)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getFeedbackRemarks(feedbackId))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getFeedbackRemarks(feedbackId))
        }
    }

    private fun saveFeedbackRemarkList(feedbackRemarkList: List<FeedbackRemark>?) {
        mExecutors.ioThread {
            mDao.saveFeedbackRemark(feedbackRemarkList)
        }
    }

    @WorkerThread
    private fun getFeedbackRemarks(feedbackId: String): List<FeedbackRemark> {
        return mDao.getFeedbackRemarkAll(feedbackId)
    }

    @WorkerThread
    private fun getFeedback(): List<Feedback>? {
        return mDao.feedbackAll
    }

    private fun saveFeedbackHeader(data: List<FeedbackHeader>?) {
        mExecutors.ioThread {
            mDao.saveFeedbackHeader(data)
        }
    }

    @WorkerThread
    private fun getFeedbackHeader(): List<FeedbackHeader>? {
        return mDao.feedbackHeaderAll
    }

    @WorkerThread
    private fun doCreateFeedbackFromNetworkRequest(bstId: String, feedbackHeader: String,
                                                   otherDetails: String): Resource<GenericApiResponse<String>>? {
        return try {
            val response = mApi.createFeedback(bstId,
                    feedbackHeader, otherDetails, mPrefRepository.apiToken()).execute()
            if (response.isSuccessful) {
                val result = response.body()
                Resource.success(result)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            Resource.error(parseErrorBody(), null)
        }
    }

    @WorkerThread
    private fun doFetchFeedbackHeaderNetworkRequest(): Resource<List<FeedbackHeader>> {
        try {
            val response = mApi
                    .fetchFeedbackHeaders(mPrefRepository.apiToken()).execute()

            return if (response.isSuccessful) {
                val result = response.body()
                saveFeedbackHeader(result?.data)
                Resource.success(result?.data)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getFeedbackHeader())
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getFeedbackHeader())
        }
    }

    @WorkerThread
    private fun doFetchFeedbackFromNetwork(): Resource<List<Feedback>> {
        try {
            val response = mApi.fetchFeedback(
                    "1",
                    "30",
                    mPrefRepository.apiToken())
                    .execute()
            return if (response.isSuccessful) {
                val resource = response.body()
                val feedbackList = resource?.data
                saveFeedbackList(feedbackList)
                Resource.success(feedbackList)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getFeedback())
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getFeedback())
        }
    }

    private fun saveFeedbackList(feedbackList: List<Feedback>?) {
        mExecutors.ioThread {
            mDao.saveFeedback(feedbackList)
        }
    }
}