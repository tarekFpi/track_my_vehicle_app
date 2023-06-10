package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import javax.inject.Inject

class ExpenseViewModel
@Inject constructor(private val mApi: WebService,
                    private val mPrefRepository: PrefRepository
) : ViewModel() {


}