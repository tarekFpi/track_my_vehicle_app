package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.implementation.v3.ISupportTicketRepository
import com.singularity.trackmyvehicle.repository.interfaces.FeedbackRepository
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/14/18.
 */
class FeedbackViewModel @Inject constructor(
        private val mFeedbackRepository: FeedbackRepository,
        private val mSupportTicketRepository: ISupportTicketRepository
) : ViewModel() {

    fun fetchFeedbackHeader(): MutableLiveData<Resource<List<FeedbackHeader>>> {
        return mFeedbackRepository.fetchExpenseHeader()
    }

    fun createFeedback(bstId: String,
                       feedbackHeader: String,
                       otherDetails: String
    ): LiveData<Resource<GenericApiResponse<String>>> {
        return mFeedbackRepository.createFeedback(bstId, feedbackHeader, otherDetails)
    }

    fun createSupport(
            vehicleId: String,
            supportCategory: String,
            subject: String,
            message: String,
            supportRequestId: String?
    ): LiveData<Resource<GenericApiResponse<String>>> {
        return mSupportTicketRepository.createSupport(vehicleId, supportCategory, subject, message,supportRequestId)
    }

    fun getFeedback(): LiveData<MutableList<Feedback>>? {
        return mFeedbackRepository.getFeedbacksAsync()
    }

    fun fetchFeedback() {
        mFeedbackRepository.fetchPaginatedFeedback()
    }


    fun fetchSinglePageFeedbackWithStatus(): LiveData<Resource<List<Feedback>>> {
        return mFeedbackRepository.fetchFeedback()
    }


    fun fetchFeedbackRemarks(feedbackId: String): LiveData<Resource<List<FeedbackRemark>>> {
        return mFeedbackRepository.fetchFeedbackRemarks(feedbackId)
    }
}
