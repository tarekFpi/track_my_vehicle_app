package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.Resource

interface FeedbackRepository {
    fun fetchExpenseHeader(): MutableLiveData<Resource<List<FeedbackHeader>>>
    fun fetchFeedback(): LiveData<Resource<List<Feedback>>>
    fun fetchFeedbackRemarks(feedbackId: String): LiveData<Resource<List<FeedbackRemark>>>
    fun createFeedback(bstId: String, feedbackHeader: String,
                       otherDetails: String): LiveData<Resource<GenericApiResponse<String>>>
    fun getFeedbacksAsync(): LiveData<MutableList<Feedback>>?
    fun fetchPaginatedFeedback()
}
