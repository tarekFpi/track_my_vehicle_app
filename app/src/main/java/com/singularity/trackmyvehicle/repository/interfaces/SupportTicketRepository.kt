package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v3.SupportTicket
import com.singularity.trackmyvehicle.network.Resource

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */

interface SupportTicketRepository {
    fun fetchSupportTicket(): MutableLiveData<Resource<List<SupportTicket>>>
    fun getSupportTicket(): LiveData<List<SupportTicket>>
}
