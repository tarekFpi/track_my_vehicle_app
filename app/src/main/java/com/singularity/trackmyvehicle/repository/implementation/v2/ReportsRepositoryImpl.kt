package com.singularity.trackmyvehicle.repository.implementation.v2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.db.dao.ReportsDao
import com.singularity.trackmyvehicle.db.dao.VehicleDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.ReportsRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/12/18.
 */

class ReportsRepositoryImpl
@Inject constructor(
        private val mDao: ReportsDao,
        private val mVehicleDao: VehicleDao,
        private val mApi: WebService,
        private val mPrefRepository: PrefRepository,
        private val mExecutors: AppExecutors
) : ReportsRepository {

    override fun fetchDistanceReport(bstid: String, date: String, type:String): MutableLiveData<Resource<List<DistanceReport>>> {
        val liveData = MutableLiveData<Resource<List<DistanceReport>>>()
        if(type != "daily") {
            liveData.postValue(Resource.error("This feature is not supported on version 2 API", null))
            return liveData
        }

        mExecutors.ioThread {
            if(mVehicleDao.getById(bstid)?.isSuspended == true) {
                liveData.postValue(Resource.error("Suspended Vehicle", null))
                return@ioThread
            }
            liveData.postValue(Resource.loading(getDistanceReport(bstid, date)))
            mExecutors.networkThread {
                val resource = doFetchDistanceReportFromNetwork(bstid, date)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchDistanceReportFromNetwork(bstid: String, date: String): Resource<List<DistanceReport>> {
        try {
            val response = mApi.fetchDistanceReport(
                    bstid,
                    date,
                    mPrefRepository.apiToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val body = response.body()
                val bstId = body?.data?.vehicle?.bstid
                val distances = body?.data?.distance
                distances?.forEach { route ->
                    route.bstId = bstId ?: ""
                }
                saveDistancesToDb(distances)
                Resource.success(distances)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getDistanceReport(bstid, date))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getDistanceReport(bstid, date))
        }
    }

    private fun saveDistancesToDb(distances: List<DistanceReport>?) {
        mExecutors.ioThread {
            mDao.saveDistanceReport(distances)
        }
    }

    @WorkerThread
    private fun getDistanceReport(bstid: String, date: String): List<DistanceReport> {
        val transformedDate = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM")
        return mDao.getDistanceReport(bstid, "%$transformedDate%")
    }

    override fun fetchSpeedReport(bstid: String, date: String): MutableLiveData<Resource<List<SpeedViolationModel>>> {
        val liveData = MutableLiveData<Resource<List<SpeedViolationModel>>>()
        mExecutors.ioThread {
            if(mVehicleDao.getById(bstid)?.isSuspended == true) {
                liveData.postValue(Resource.error("Suspended Vehicle", null))
                return@ioThread
            }
            liveData.postValue(Resource.loading(getSpeedReport(bstid, date)))
            mExecutors.networkThread {
                val resource = doFetchSpeedReportFromNetwork(bstid, date)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchSpeedReportFromNetwork(bstid: String, date: String): Resource<List<SpeedViolationModel>> {
        try {
            val response = mApi.fetchSpeedReport(
                    bstid,
                    date,
                    mPrefRepository.apiToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                val body = response.body()
                val bstId = body?.data?.vehicle?.bstid
                val speedAlerts = body?.data?.speedAlert
                speedAlerts?.forEach { speedAlert ->
                    speedAlert.bstId = bstId ?: ""
                }
                saveSpeedAlertsToDb(speedAlerts)
                Resource.success(getSpeedReport(bstid, date))
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getSpeedReport(bstid, date))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getSpeedReport(bstid, date))
        }
    }

    private fun saveSpeedAlertsToDb(speedAlerts: List<SpeedAlertReport>?) {
        mExecutors.ioThread {
            mDao.saveSpeedReport(speedAlerts)
        }
    }

    @WorkerThread
    private fun getSpeedReport(bstid: String, date: String): List<SpeedViolationModel> {
        val dateStart = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM-01 00:00:00")
        val dateEnd = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM-31 00:00:00")
        return mDao.getSpeedViolation(bstid, dateStart, dateEnd)
    }

    override fun fetchExpenseHeader(): MutableLiveData<Resource<List<ExpenseHeader>>> {
        val liveData = MutableLiveData<Resource<List<ExpenseHeader>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getExpenseHeader()))
            mExecutors.networkThread {
                val resource = doFetchExpenseHeaderFromNetwork()
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchExpenseHeaderFromNetwork(): Resource<List<ExpenseHeader>> {
        try {
            val response = mApi
                    .fetchExpenseHeader(mPrefRepository.apiToken())
                    .execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                val body = response.body()
                saveExpenseHeaderToDb(body?.data)
                Resource.success(body?.data)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getExpenseHeader())
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getExpenseHeader())
        }
    }

    private fun saveExpenseHeaderToDb(data: List<ExpenseHeader>?) {
        mExecutors.ioThread {
            mDao.saveExpenseHeader(data)
        }
    }

    @WorkerThread
    private fun getExpenseHeader(): List<ExpenseHeader> {
        return mDao.expenseHeader.toList()
    }

    override fun postCreateExpense(bstId: String, date: String, expense: String, amount: Int,
                                   details: String): LiveData<Resource<GenericApiResponse<String>>> {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        liveData.postValue(Resource.loading(null))
        mExecutors.networkThread {
            val resource = doPostExpenseToNetwork(bstId, date, expense, amount, details)
            liveData.postValue(resource)
        }
        return liveData
    }

    @WorkerThread
    private fun doPostExpenseToNetwork(bstId: String, date: String, expense: String,
                                       amount: Int, details: String): Resource<GenericApiResponse<String>> {
        try {
            val response = mApi.postExpense(
                    bstId,
                    date,
                    expense,
                    amount,
                    details,
                    mPrefRepository.apiToken()
            ).execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                val body = response.body()
                Resource.success(body)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, null)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), null)
        }
    }

    /**
     * date -> yyyy-MM-dd
     */
    override fun fetchExpense(bstid: String, date: String): MutableLiveData<Resource<List<Expense>>> {
        val liveData = MutableLiveData<Resource<List<Expense>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getExpenseReport(bstid, date)))
            mExecutors.networkThread {
                val resource = doFetchPreviousExpenseFromNetwork(bstid, date)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchPreviousExpenseFromNetwork(bstid: String, date: String): Resource<List<Expense>> {
        try {
            val response = mApi
                    .fetchPreviousExpense(
                            bstid,
                            date,
                            mPrefRepository.apiToken()
                    ).execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                val body = response.body()
                body?.data?.forEach { expense ->
                    expense?.bstid = bstid
                }
                saveExpenseToDb(body?.data)
                Resource.success(body?.data)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getExpenseReport(bstid, date))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getExpenseReport(bstid, date))
        }
    }

    private fun saveExpenseToDb(data: List<Expense>?) {
        mExecutors.ioThread {
            mDao.saveExpense(data)
        }
    }

    /**
     * must be called from background thread
     * date -> yyyy-MM-dd
     */
    @WorkerThread
    private fun getExpenseReport(bstid: String, dateString: String): List<Expense> {
        val date = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM")
        return mDao.getExpense(bstid, "%$date%")
    }

    override fun fetchSpeedViolations(bstid: String, date: String): MutableLiveData<Resource<List<SpeedAlertReport>>> {
        val liveData = MutableLiveData<Resource<List<SpeedAlertReport>>>()
        mExecutors.ioThread {
            liveData.postValue(Resource.loading(getSpeedViolations(bstid, date)))
            mExecutors.networkThread {
                val resource = doFetchSpeedAlertReportFromNetwork(bstid, date)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchSpeedAlertReportFromNetwork(bstid: String, date: String): Resource<List<SpeedAlertReport>> {
        try {
            val response = mApi
                    .fetchSpeedReport(
                            bstid,
                            date,
                            mPrefRepository.apiToken()
                    ).execute() ?: return Resource.error("Please Check Your Network Connection", null)

            return if (response.isSuccessful) {
                val body = response.body()
                val bstId = body?.data?.vehicle?.bstid
                val speedAlerts = body?.data?.speedAlert
                speedAlerts?.forEach { speedAlert ->
                    speedAlert.bstId = bstId ?: ""
                }
                saveSpeedAlertsToDb(speedAlerts)
                Resource.success(speedAlerts)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getSpeedViolations(bstid, date))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getSpeedViolations(bstid, date))
        }
    }

    private fun getSpeedViolations(bstid: String, date: String): List<SpeedAlertReport> {
        val dateStart = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM-01 00:00:00")
        val dateEnd = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).toString("yyyy-MM-31 00:00:00")
        return mDao.getSpeedViolationReports(bstid, dateStart, dateEnd).toList()
    }

}