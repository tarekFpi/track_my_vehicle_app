package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.ReportsRepository
import org.joda.time.DateTime
import javax.inject.Inject


/**
 * Created by Sadman Sarar on 8/3/18.
 */
class ReportsViewModel
@Inject constructor(
        private val mRepository: ReportsRepository,
        private val mPrefRepository: PrefRepository
) : ViewModel() {

    private fun fetchDistanceReport(bstid: String, date: DateTime, type: String= "daily"): MutableLiveData<Resource<List<DistanceReport>>> {
        return mRepository.fetchDistanceReport(bstid, date.toString("yyyy-MM-dd"),type)
    }

    fun fetchMonthlyReport(date: DateTime): MutableLiveData<Resource<List<DistanceReport>>> {
        return mRepository.fetchDistanceReport("",date.toString("yyyy-MM-dd"),"monthly")
    }

    fun fetchCurrentVehicleDistanceReport(date: DateTime): MutableLiveData<Resource<List<DistanceReport>>> {
        return fetchDistanceReport(mPrefRepository.currentVehicle(), date)
    }

    fun fetchCurrentVehicleDistanceHourlyDistanceReport(date: DateTime): MutableLiveData<Resource<List<DistanceReport>>> {
        return fetchDistanceReport(mPrefRepository.currentVehicle(), date, "hourly")
    }

    private fun fetchSpeedReport(bstid: String, date: DateTime): MutableLiveData<Resource<List<SpeedViolationModel>>> {
        return mRepository.fetchSpeedReport(bstid, date.toString("yyyy-MM-dd"))
    }

    fun fetchCurrentVehicleSpeedReport(date: DateTime): MutableLiveData<Resource<List<SpeedViolationModel>>> {
        return fetchSpeedReport(mPrefRepository.currentVehicle(), date)
    }

    fun fetchExpenseHeader(): MutableLiveData<Resource<List<ExpenseHeader>>> {
        return mRepository.fetchExpenseHeader()
    }

    fun postCreateExpense(bstId: String, date: String, expense: String, amount: Int, details: String): LiveData<Resource<GenericApiResponse<String>>> {
        return mRepository.postCreateExpense(bstId, date, expense, amount, details)
    }

    fun postCreateExpenseForCurrentVehicle(date: String, expense: String, amount: Int, details: String): LiveData<Resource<GenericApiResponse<String>>> {
        return mRepository.postCreateExpense(mPrefRepository.currentVehicle(), date, expense, amount, details)
    }


    private fun fetchExpense(bstid: String, date: String): MutableLiveData<Resource<List<Expense>>> {
        return mRepository.fetchExpense(bstid, date)
    }

    fun fetchCurrentVehicleExpense(date: String): MutableLiveData<Resource<List<Expense>>> {
        return fetchExpense(mPrefRepository.currentVehicle(), date)
    }

    fun fetchCurrentVehicleSpeedViolatoint(dateTime: DateTime): MutableLiveData<Resource<List<SpeedAlertReport>>> {
        return fetchCurrentVehicleSpeedViolatoint(mPrefRepository.currentVehicle(), dateTime)
    }

    private fun fetchCurrentVehicleSpeedViolatoint(bstid: String, dateTime: DateTime): MutableLiveData<Resource<List<SpeedAlertReport>>> {
        return mRepository.fetchSpeedViolations(bstid, dateTime?.toString("yyyy-MM-dd"))
    }


}