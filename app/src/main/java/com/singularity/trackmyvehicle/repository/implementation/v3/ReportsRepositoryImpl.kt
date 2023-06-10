package com.singularity.trackmyvehicle.repository.implementation.v3

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.db.dao.TerminalAggregatedDataDao
import com.singularity.trackmyvehicle.db.dao.TerminalDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.*
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.ReportsRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/12/18.
 */

class ReportsRepositoryImpl
@Inject constructor(
        private val mWebService: WebService,
        private val mTerminalDao: TerminalDao,
        private val mAppExecutors: AppExecutors,
        private val mTerminalAggregatedDataDao: TerminalAggregatedDataDao
) : ReportsRepository {

    private fun fetchDailyDistanceReport(
            bstId: String,
            date: DateTime
    ): MutableLiveData<Resource<List<DistanceReport>>> {
        val liveData = MutableLiveData<Resource<List<DistanceReport>>>()
        mAppExecutors.ioThread {
            val terminal = mTerminalDao.getById(bstId.removePrefix(Terminal.PREFIX))
            if (terminal == null) {
                liveData.postValue(Resource.error("Terminal not found", null))
                return@ioThread
            }
            if (terminal.isSuspended()) {
                liveData.postValue(Resource.error("Suspended Vehicle", null))
                return@ioThread
            }
            val offLineOutput = mTerminalAggregatedDataDao.getTerminalAggregatedDataByRangeTerminalList(
                    date.withDayOfMonth(1).withTimeAtStartOfDay().millis,
                    date.withDayOfMonth(date.dayOfMonth().withMaximumValue().dayOfMonth).withTime(
                            23, 59, 59, 999).millis,
                    "daily", terminal.terminalID.toString())

            liveData.postValue(Resource.loading(offLineOutput.map { convertToDistanceReport(it) }))

            val resource = doFetchDailyDistanceReport(terminal.terminalID, date)
            liveData.postValue(resource)

        }
        return liveData
    }

    @WorkerThread
    private fun doFetchDailyDistanceReport(terminalID: Int,
                                           date: DateTime): Resource<List<DistanceReport>> {
        try {
            val response = mWebService.getTerminalAggregatedGroupedData(
                    ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
                    date.withDayOfMonth(1).withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                    date.withDayOfMonth(date.dayOfMonth().withMaximumValue().dayOfMonth).withTime(
                            23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss"),
                    terminalID,
                    3
            ).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            if (body?.isFailed() == false) {
                val data = body.response?.data
                val list = mutableListOf<DistanceReport>()
                data?.forEach {
                    it.aggregatedDurationStartFrom = DateTime.parse(it.terminalDataMinutelyTimeFrom,
                            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).withTimeAtStartOfDay()
                    it.aggregatedDurationType = "daily"
                    val model = convertToDistanceReport(it)
                    list.add(model)
                }
                mTerminalAggregatedDataDao.save(data)
                return Resource.success(list)
            } else if (body?.error?.code == 3) {
                return Resource.success(listOf())
            } else {
                return Resource.error(body?.error?.description ?: "Something went wrong", null)
            }
        } catch (ex: Exception) {
            return Resource.error(ex.message ?: "Something went wrong", null)
        }
    }

    private fun fetchMonthlyDistanceReport(
            date: DateTime
    ): MutableLiveData<Resource<List<DistanceReport>>> {
        val liveData = MutableLiveData<Resource<List<DistanceReport>>>()
        mAppExecutors.ioThread {
            val offLineOutput = getMonthlyReport(date)

            liveData.postValue(Resource.loading(offLineOutput.map { convertToDistanceReport(it) }))

            val resource = doFetchMonthlyDistanceReport(date)
            liveData.postValue(resource)

        }
        return liveData
    }

    private fun getMonthlyReport(
            date: DateTime): List<TerminalAggregatedData> {
        return mTerminalAggregatedDataDao.getTerminalAggregatedDataAllByRangeTerminalList(
                date.withDayOfMonth(1).withTimeAtStartOfDay().millis,
                date.withDayOfMonth(date.dayOfMonth().withMaximumValue().dayOfMonth).withTime(
                        23, 59, 59, 999).millis,
                "monthly")
    }

    @WorkerThread
    private fun doFetchMonthlyDistanceReport(date: DateTime): Resource<List<DistanceReport>> {
        try {
            val response = mWebService.getTerminalAggregatedGroupedDataAll(
                    ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
                    date.withDayOfMonth(1).withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                    date.withDayOfMonth(date.dayOfMonth().withMaximumValue().dayOfMonth).withTime(
                            23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss"),
                    2
            ).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            if (body?.isFailed() == false) {
                val data = body.response?.data
                val list = mutableListOf<DistanceReport>()
                data?.forEach {
                    it.aggregatedDurationStartFrom = DateTime.parse(it.terminalDataMinutelyTimeFrom,
                            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).withDayOfMonth(1)
                            .withTimeAtStartOfDay()
                    it.aggregatedDurationType = "monthly"
                    val model = convertToDistanceReport(it)
                    list.add(model)
                }
                mTerminalAggregatedDataDao.save(data)
                return Resource.success(list)
            } else if (body?.error?.code == 3) {
                return Resource.success(listOf())
            } else {
                return Resource.error(body?.error?.description ?: "Something went wrong",
                        getMonthlyReport(date).map { convertToDistanceReport(it) })
            }
        } catch (ex: Exception) {
            return Resource.error(ex.message ?: "Something went wrong",
                    getMonthlyReport(date).map { convertToDistanceReport(it) })
        }
    }

    private fun convertToDistanceReport(
            it: TerminalAggregatedData, daily: Boolean = true): DistanceReport {
        val model = DistanceReport()
        model.bstId = Terminal.PREFIX + it.terminalAssignmentCode
        model.date = if (daily) it.aggregatedDurationStartFrom.toString(
                "yyyy-MM-dd") else it.aggregatedDurationStartFrom.toString("yyyy-MM-dd HH:mm:ss")
        model.km = if (daily) (((it.terminalDataMinutelyDistanceMeter?.toFloatOrNull()
                ?: 0F) / 1000).toString()) else it.terminalDataMinutelyDistanceMeter
        model.vrn = it.carrierRegistrationNumber
        model.TerminalAssignmentIsSuspended = it.TerminalAssignmentIsSuspended
        return model
    }

    private fun fetchHourlyDistanceReport(
            bstId: String,
            date: DateTime
    ): MutableLiveData<Resource<List<DistanceReport>>> {
        val liveData = MutableLiveData<Resource<List<DistanceReport>>>()
        mAppExecutors.ioThread {
            liveData.postValue(Resource.loading(null))
            val terminal = mTerminalDao.getById(bstId.removePrefix(Terminal.PREFIX))
            if (terminal == null) {
                liveData.postValue(Resource.error("Terminal not found", null))
                return@ioThread
            }
            if (terminal.isSuspended()) {
                liveData.postValue(Resource.error("Suspended Vehicle", null))
                return@ioThread
            }
            val offLineOutput = mTerminalAggregatedDataDao.getTerminalAggregatedDataByRangeTerminalList(
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(23, 59, 59, 999).millis,
                    "hourly", terminal.terminalID.toString())

            liveData.postValue(
                    Resource.loading(offLineOutput.map { convertToDistanceReport(it, false) }))

            val resource = doFetchHourlyDistanceReport(terminal?.terminalID ?: 0, date)
            liveData.postValue(resource)
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchHourlyDistanceReport(terminalId: Int,
                                            date: DateTime): Resource<List<DistanceReport>> {
        try {
            val response = mWebService.getTerminalAggregatedGroupedData(
                    ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
                    date.toString("yyyy-MM-dd HH:mm:ss"),
                    date.withTime(23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss"),
                    terminalId,
                    4
            ).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            if (body?.isFailed() == false) {
                val data = body.response?.data
                data?.forEach {
                    it.aggregatedDurationStartFrom = DateTime.parse(it.terminalDataMinutelyTimeFrom,
                            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
                    it.aggregatedDurationType = "hourly"
                }
                mTerminalAggregatedDataDao.save(data)
                val list = mutableListOf<DistanceReport>()
                data?.forEach {
                    val model = convertToDistanceReport(it, false)
                    list.add(model)
                }
                return Resource.success(list)
            } else {
                return Resource.error(body?.error?.description ?: "Something went wrong", null)
            }
        } catch (ex: Exception) {
            return Resource.error(ex.message ?: "Something went wrong", null)
        }
    }

    override fun fetchDistanceReport(
            bstid: String,
            date: String,
            type: String
    ): MutableLiveData<Resource<List<DistanceReport>>> {

        return if (type == "daily") {

            fetchDailyDistanceReport(bstid, DateTime.parse(date))

        } else if (type == "monthly") {

            fetchMonthlyDistanceReport(DateTime.parse(date))
        } else {

            fetchHourlyDistanceReport(bstid, DateTime.parse(date))
        }
    }

    private fun isTerminalSuspended(bstid: String): Boolean {
        val terminal = mTerminalDao.getById(bstid.removePrefix(Terminal.PREFIX))
        return terminal?.isSuspended() == true
    }

    override fun fetchSpeedReport(bstid: String,
                                  date: String): MutableLiveData<Resource<List<SpeedViolationModel>>> {
        return MutableLiveData()
    }

    override fun fetchExpenseHeader(): MutableLiveData<Resource<List<ExpenseHeader>>> {
        return MutableLiveData()
    }

    override fun postCreateExpense(bstId: String, date: String, expense: String, amount: Int,
                                   details: String): LiveData<Resource<GenericApiResponse<String>>> {
        return MutableLiveData()
    }

    override fun fetchExpense(bstid: String,
                              date: String): MutableLiveData<Resource<List<Expense>>> {
        return MutableLiveData()
    }

    override fun fetchSpeedViolations(bstid: String,
                                      date: String): MutableLiveData<Resource<List<SpeedAlertReport>>> {
        return MutableLiveData()
    }
}