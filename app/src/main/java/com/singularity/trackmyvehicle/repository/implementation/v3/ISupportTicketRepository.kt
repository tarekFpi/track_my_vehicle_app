package com.singularity.trackmyvehicle.repository.implementation.v3

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonSyntaxException
import com.singularity.trackmyvehicle.db.dao.SupportTicketCategoryDao
import com.singularity.trackmyvehicle.db.dao.SupportTicketDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.SupportTicket
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.FeedbackRepository
import com.singularity.trackmyvehicle.repository.interfaces.SupportTicketRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import org.joda.time.DateTime
import java.net.SocketTimeoutException
import javax.inject.Inject


class ISupportTicketRepository @Inject constructor(
        private val mApi: WebService,
        private val mExecutor: AppExecutors,
        private val mNetworkAvailabilityChecker: NetworkAvailabilityChecker,
        private val mDao: SupportTicketDao,
        private val mCategoryDao: SupportTicketCategoryDao
) : SupportTicketRepository, FeedbackRepository {

    companion object {
        const val SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }

    override fun fetchSupportTicket(): MutableLiveData<Resource<List<SupportTicket>>> {
        val liveData = MutableLiveData<Resource<List<SupportTicket>>>()

        mExecutor.ioThread {
            liveData.postValue(Resource.loading(mDao.allSupportTicket))
            liveData.postValue(doFetchSupportTickets())
        }

        return liveData
    }

    @WorkerThread
    private fun doFetchSupportTickets(): Resource<List<SupportTicket>> {
        try {
            val response = mApi.getSupportRequestList(
                    ENDPOINTS.SUPPORT_REQUEST_RESPONSE_LIST,
                    DateTime.now().withTimeAtStartOfDay().minusYears(1).toString(
                            SERVER_DATE_FORMAT)
            ).execute()

            return if (response.isSuccessful && response.body()?.isFailed() == false) {
                mDao.refresh(response.body()?.response)
                Resource.success(mDao.allSupportTicket)
            } else {
                Resource.error(
                        response.body()?.error?.description ?: "Something went wrong",
                        mDao.allSupportTicket)
            }
        } catch (ex: Exception) {
            return Resource.error(
                    if (!mNetworkAvailabilityChecker.isNetworkAvailable()) {
                        "Network error"
                    } else if (ex is SocketTimeoutException) {
                        "Socket Timeout"
                    } else if (ex is JsonSyntaxException) {
                        "Malformed Json"
                    } else {
                        "Something went wrong"
                    }
                    , mDao.allSupportTicket)
        }
    }

    override fun getSupportTicket(): LiveData<List<SupportTicket>> {
        return mDao.supportTicket
    }

    override fun fetchExpenseHeader(): MutableLiveData<Resource<List<FeedbackHeader>>> {
        val output = MutableLiveData<Resource<List<FeedbackHeader>>>()
        mExecutor.ioThread {
            try {
                val response = mApi.getSupportRequestCategoryList(
                        ENDPOINTS.SUPPORT_REQUEST_CATEGORY_LIST)
                        .execute()
                if (response.isSuccessful && response.body()?.isFailed() == false) {
                    mCategoryDao.refresh(response?.body()?.response)
                    output.postValue(Resource.success(
                            mCategoryDao.allSupportRequestCategory.map { it.toFeedbackHeader() }))
                } else {
                    output.postValue(Resource.error(
                            response.body()?.error?.description ?: "Something went wrong",
                            mCategoryDao.allSupportRequestCategory.map { it.toFeedbackHeader() }))
                }
            } catch (ex: Exception) {
                output.postValue(Resource.error(ex.message ?: "Something went wrong",
                        mCategoryDao.allSupportRequestCategory.map { it.toFeedbackHeader() }))
            }
        }
        return output
    }

    override fun fetchFeedback(): LiveData<Resource<List<Feedback>>> {
        val output = Transformations.map(
                fetchSupportTicket()) { input: Resource<List<SupportTicket>>? ->
            val dataOutput = input?.data?.sortedBy { it.supportRequestResponseTime }
                    ?.distinctBy { it.supportRequestSubject }
                    ?.map { it.toFeedback(input.data) }?.sortedByDescending { it.feedbackId }
            when (input?.status) {
                Status.LOADING -> Resource.loading(dataOutput)
                Status.SUCCESS -> Resource.success(dataOutput)
                Status.ERROR -> Resource.error(input.message ?: "Something went wrong", dataOutput)
                else -> {
                    Resource.error<List<Feedback>>("Error", null)
                }
            }
        }

        return output
    }

    override fun fetchFeedbackRemarks(
            feedbackId: String): LiveData<Resource<List<FeedbackRemark>>> {
        val output = Transformations.map(
                fetchSupportTicket()) { input: Resource<List<SupportTicket>>? ->
            val dataOutput = input?.data?.filter { it.supportRequestID == feedbackId }
                    ?.map { it.toFeedbackRemark() }
            when (input?.status) {
                Status.LOADING -> Resource.loading(dataOutput)
                Status.SUCCESS -> Resource.success(dataOutput)
                Status.ERROR -> Resource.error(input.message ?: "Something went wrong", dataOutput)
                else -> {
                    Resource.error<List<FeedbackRemark>>("Error", null)
                }
            }
        }

        return output
    }

    override fun createFeedback(bstId: String, feedbackHeader: String,
                                otherDetails: String): LiveData<Resource<GenericApiResponse<String>>> {
        return MutableLiveData<Resource<GenericApiResponse<String>>>()
    }

    override fun getFeedbacksAsync(): LiveData<MutableList<Feedback>>? {
        return Transformations.map(getSupportTicket()) { input: List<SupportTicket>? ->
            val dataOutput = input?.sortedBy { it.supportRequestResponseTime }
                    ?.distinctBy { it.supportRequestSubject }
                    ?.map { it.toFeedback(input) }
            dataOutput?.toMutableList()
        }
    }

    override fun fetchPaginatedFeedback() {
        fetchSupportTicket()
    }

    fun createSupport(
            vehicleId: String,
            supportCategory: String,
            subject: String,
            message: String,
            supportRequestId: String?
    ): LiveData<Resource<GenericApiResponse<String>>> {

        val output = MutableLiveData<Resource<GenericApiResponse<String>>>()

        output.postValue(Resource.loading())
        mExecutor.ioThread {
            try {
                val response = mApi.createSupportRequest(
                        ENDPOINTS.SUPPORT_REQUEST_CREATE,
                        subject,
                        message,
                        supportCategory,
                        vehicleId,
                        supportRequestId
                ).execute()
                if (response.isSuccessful && response.body()?.isFailed() == false) {
                    output.postValue(Resource.success(GenericApiResponse<String>().apply {
                        this.appMessage = "Support request created"
                        this.userMessage = "Support request created"
                    }))
                } else {
                    output.postValue(Resource.error("Support request failed to create"))
                }
            } catch (ex: Exception) {
                output.postValue(Resource.error(ex.meaningfulError()))
            }
        }

        return output

    }
}

fun Exception.meaningfulError() : String {
    return if(this is SocketTimeoutException) {
        "Network error or server took a long time to response"
    } else if(this  is JsonSyntaxException) {
        "Server returned Malformed JSON"
    }else {
        this.message ?: "Something went wrong"
    }
}