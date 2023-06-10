package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.network.Resource

interface ReportsRepository {
    fun fetchDistanceReport(bstid: String, date: String, type: String= "daily"): MutableLiveData<Resource<List<DistanceReport>>>
    fun fetchSpeedReport(bstid: String, date: String): MutableLiveData<Resource<List<SpeedViolationModel>>>
    fun fetchExpenseHeader(): MutableLiveData<Resource<List<ExpenseHeader>>>
    fun postCreateExpense(bstId: String, date: String, expense: String, amount: Int, details: String): LiveData<Resource<GenericApiResponse<String>>>
    fun fetchExpense(bstid: String, date: String): MutableLiveData<Resource<List<Expense>>>
    fun fetchSpeedViolations(bstid: String, date: String): MutableLiveData<Resource<List<SpeedAlertReport>>>
}
