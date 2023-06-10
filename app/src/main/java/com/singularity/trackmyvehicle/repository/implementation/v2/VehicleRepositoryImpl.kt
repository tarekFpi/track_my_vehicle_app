package com.singularity.trackmyvehicle.repository.implementation.v2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import android.util.Log
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.db.dao.VehicleDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleRouteResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.entity.VehicleRoutePolyline
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.PaginatedCallback
import com.singularity.trackmyvehicle.network.PaginatedVehicleFetcher
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.RateLimiter
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Sadman Sarar on 8/3/18.
 * Repository class - uses NetworkBoundResource to load data from API
 */
@Singleton
class VehicleRepositoryImpl
@Inject constructor(
        private val mDao: VehicleDao,
        private val mApi: WebService,
        private val mVehicleFetcher: PaginatedVehicleFetcher,
        private val mPrefRepository: PrefRepository,
        private val mExecutors: AppExecutors
) : VehicleRepository, PaginatedCallback<Vehicle> {

    val repoListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    init {
        mVehicleFetcher.callback = this
    }

    override fun fetch(lazy: Boolean) {
        if (mVehicleFetcher.isRunning)
            return
        if (lazy && !mVehicleFetcher.shouldFetchLazy()) {
            return
        }
        mVehicleFetcher.fetch()
    }

    override fun getVehicles(maxCount: Int): LiveData<List<Terminal>> {
        if (maxCount == 0) {
            return Transformations.map(mDao.vehicle) {
                var list = mutableListOf<Terminal>()
                for (i in it) {
                    var model = Terminal()
                    model.terminalID = i.bid.toIntOrNull() ?: 0
                    model.carrierRegistrationNumber = i.vrn
                    model.terminalAssignmentCode = i?.bid
                    model.terminalAssignmentIsSuspended = if(i?.isSuspended == true) "1" else "0"
                    list.add(model)
                }
                return@map list
            }
        }
        return Transformations.map(mDao.getVehicleWithMaxCount(maxCount)) {
            var list = mutableListOf<Terminal>()
            for (i in it) {
                var model = Terminal()
                model.terminalID = i.bid.toIntOrNull() ?: 0
                model.carrierRegistrationNumber = i.vrn
                model.terminalAssignmentCode = i?.bid
                model.terminalAssignmentIsSuspended = if(i?.isSuspended == true) "1" else "0"
                list.add(model)
            }
            return@map list
        }
    }

    override fun getVehicleStatus(bstId: String): LiveData<Resource<VehicleStatus>> {
        val liveData = MutableLiveData<Resource<VehicleStatus>>()
        mExecutors.ioThread {
            val vehicle = mDao.getById(bstId)
            if(vehicle?.isSuspended == true) {
                val status = VehicleStatus().apply {
                    speed = "--"
                    engineStatus = "--"
                    location = LocationResponse().apply {
                        place = "--"
                        direction = "0"
                        longitude = ""
                        latitude = ""
                        distance = ""
                    }
                    updatedAt = ""
                    vrn = vehicle.vrn
                    bstid = vehicle.bstid
                    bid = vehicle.bid.toIntOrNull() ?: 0
                }
                liveData.postValue(Resource.success(status))
                return@ioThread
            }
            liveData.postValue(Resource.loading(null))
            mExecutors.networkThread {
                val resource = doFetchVehicleStatusFromNetwork(bstId)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    @WorkerThread
    private fun doFetchVehicleStatusFromNetwork(bstId: String): Resource<VehicleStatus> {
        try {
            val response = mApi
                    .fetchVehicleStatus(
                            bstId,
                            mPrefRepository.apiToken()
                    ).execute()
                    ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val responseBody = response.body()
                Resource.success(responseBody?.data)
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

    override fun fetchVehicleRoutes(bstid: String,
                                    date: String): MutableLiveData<Resource<List<VehicleRoute>>> {
        val liveData = MutableLiveData<Resource<List<VehicleRoute>>>()
        mExecutors.ioThread {
            if(mDao.getById(bstid)?.isSuspended == true) {
                liveData.postValue(Resource.error("Suspended Vehicle"))
                return@ioThread
            }
            liveData.postValue(Resource.loading(getVehicleRoutes(bstid, date)))
            mExecutors.networkThread {
                val resource = doFetchVehicleRoutesFromNetwork(bstid, date)
                liveData.postValue(resource)
            }
        }
        return liveData
    }

    override fun fetchCurrentRoutesVehicle(script: String, vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>> {
        val liveData = MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>>()
        /*mExecutors.networkThread {
            val resource = fetchVehicleRoutes(vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
            liveData.postValue(resource)
        }*/
        return liveData
    }

    private fun doFetchRoutesVehicleRoutesFromNetwork(script : String, vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): Resource<List<VehicleRouteTerminalData.VehicleRouteSuccessResponse.VehicleRouteData>>? {
        try {
            val response = mApi
                    .fetchVehicleRoutesResponse(
                            script,
                            vehicleRouteMode,
                            terminalDataTimeFrom,
                            terminalDataTimeTo,
                            terminalId,
                            mPrefRepository.apiToken()
                    ).execute()
                    ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val body = response.body()
                val routes = body?.vehiclesRouteSuccessResponse?.vehicleRouteData

                Resource.success(routes)
            }else{
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage)
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody())
        }
    }

    @WorkerThread
    private fun doFetchVehicleRoutesFromNetwork(bstid: String,
                                                date: String): Resource<List<VehicleRoute>> {
        try {
            val response = mApi
                    .fetchVehicleRoutes(
                            bstid,
                            date,
                            mPrefRepository.apiToken()
                    ).execute()
                    ?: return Resource.error("Please Check Your Network Connection", null)
            return if (response.isSuccessful) {
                val body = response.body()
                val bstId = body?.data?.vehicle?.bstid
                val routes = body?.data?.route
                routes?.forEach { route ->
                    route.bstId = bstId ?: ""
                }
                savePolyLine(bstId, date, body)
                if (routes != null) {
                    saveVehicleRouteToDb(routes.toList())
                }
                Resource.success(routes)
            } else {
                val userMessage = parseErrorBody(response)
                Resource.error(userMessage, getVehicleRoutes(bstid, date))
            }
        } catch (exception: Exception) {
            this.log(exception.message)
            FirebaseCrashlytics.getInstance().recordException(exception)
            return Resource.error(parseErrorBody(), getVehicleRoutes(bstid, date))
        }
    }


    /**
     * Saves the polyline for vehicle and date to the route_polylines table
     */
    private fun savePolyLine(bstId: String?, date: String,
                             body: GenericApiResponse<VehicleRouteResponse>?) {
        val polyline = VehicleRoutePolyline()
        polyline.bstId = bstId ?: ""
        polyline.date = date
        polyline.polyline = body?.data?.polyline
        mDao.save(polyline)
    }

    private fun saveVehicleRouteToDb(routes: List<VehicleRoute>) {
        mExecutors.ioThread {
            mDao.saveRoute(routes)
        }
    }

    @WorkerThread
    private fun getVehicleRoutes(bstid: String, date: String): List<VehicleRoute> {
        return mDao.getRoutes(bstid, "%$date%")
    }

    override fun fetchEnded(data: List<Vehicle>) {
        if (mPrefRepository.apiToken().isEmpty()) {
            return
        }
        mExecutors.ioThread {
            mDao.save(data)
            mExecutors.mainThread {
                if (mPrefRepository.currentVehicle().isEmpty() && data.isNotEmpty()) {
                    val latest = data.sortedByDescending { it.bid }[0]
                    mPrefRepository.changeCurrentVehicle(latest.bstid, latest.vrn, latest.bid)
                }
            }
        }
    }

    override fun fetchFailed(msg: String) {
        Log.d("TAG", msg)
    }

    override fun getCurrentVehicle(completion: (Terminal?) -> Unit) {
        mExecutors.ioThread {
            val vehicle = getCurrentVehicleById()
            val terminal = Terminal()
            terminal.carrierRegistrationNumber = vehicle?.vrn
            terminal.terminalID = vehicle?.bid?.toIntOrNull() ?: 0
            terminal.terminalAssignmentCode = vehicle?.bstid?.removePrefix("bst")
            terminal.sim = vehicle?.sim ?: ""
            terminal.expiryDate = vehicle?.expiryDate ?: ""
            terminal.dueAmount = vehicle?.dueAmount ?: ""
            terminal.terminalAssignmentIsSuspended = if(vehicle?.isSuspended == true) "1" else "0"
            mExecutors.mainThread {
                completion(terminal)
            }
        }
    }

    @WorkerThread
    private fun getCurrentVehicleById(): Vehicle? {
        return mDao.getById(mPrefRepository.currentVehicle())
    }

    override fun getVeicleRoutePolyLineAsync(bstId: String, date: DateTime,
                                             completion: (VehicleRoutePolyline?) -> Unit) {
        mExecutors.ioThread {
            val data = mDao.getRoutePolyline(bstId, date.toString("dd-MM-yyyy"))
            mExecutors.mainThread {
                completion(data)
            }
        }
    }

    @WorkerThread
    override fun getVeicleRoutePolyLine(bstId: String, date: DateTime): VehicleRoutePolyline? {
        return mDao.getRoutePolyline(bstId, date.toString("yyyy-MM-dd"))
    }

    override fun getTodaysTravelledDistance(bId: String): LiveData<TerminalAggregatedData> {
        // API Does not support
        val mutableLiveData = MutableLiveData<TerminalAggregatedData>()
        mutableLiveData.postValue(null)
        return mutableLiveData
    }

    override fun fetchTodaysTravelledDistance(bId: String) {
        // API Does not support
    }

    override fun getTodaysTravelledAllDistance(): LiveData<List<TerminalAggregatedData>> {
        return MutableLiveData<List<TerminalAggregatedData>>()
    }

    override fun selectNextVehicle(direction: Int) {
        val current = mPrefRepository.currentVehicle()
        mExecutors.ioThread {
            val terminal = mDao.vehicleSync
            var index = terminal.indexOfFirst { it.bstid == current }
            if (index >= 0) {
                index = if(direction ==0) {(index + 1)} else {(index - 1 + terminal.size)} % terminal.size
                val newVehicle = terminal.get(index)
                mExecutors.mainThread {
                    mPrefRepository.changeCurrentVehicle(newVehicle.bstid, newVehicle.vrn,
                            newVehicle.bid)
                }
            }
        }
    }
}
