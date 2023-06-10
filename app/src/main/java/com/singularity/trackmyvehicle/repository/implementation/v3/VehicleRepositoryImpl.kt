package com.singularity.trackmyvehicle.repository.implementation.v3

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.libraries.places.internal.it
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.singularity.trackmyvehicle.db.dao.TerminalAggregatedDataDao
import com.singularity.trackmyvehicle.db.dao.TerminalDao
import com.singularity.trackmyvehicle.db.dao.TerminalDataMinutelyDao
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.TerminalDataMinutely
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleRouteTerminalData
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.model.entity.VehicleRoutePolyline
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.utils.log
import com.singularity.trackmyvehicle.utils.parseErrorBody
import com.singularity.trackmyvehicle.utils.polylineDecoder.Point
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Sadman Sarar on 8/3/18.
 * Repository class - uses NetworkBoundResource to load data from API
 */
@Singleton
class VehicleRepositoryImpl
@Inject constructor(
        private val mWebService: WebService,
        private val mDao: TerminalDao,
        private val mTerminalAggregatedDataDao: TerminalAggregatedDataDao,
        private val mRouteDao: TerminalDataMinutelyDao,
        private val mPrefRepository: PrefRepository,
        private val mAppExecutors: AppExecutors,
        private val networkAvailability: NetworkAvailabilityChecker
) : VehicleRepository {

    var lastFetched: DateTime? = null

    override fun fetch(lazy: Boolean) {
        if (! lazy || shouldFetch()) {
            mAppExecutors.networkThread {
                doFetch()
            }
        }
    }

    private fun doFetch(): Status {
        if (IS_REQUEST_ACTIVE)
            return Status.ONGOING

        return try {
            IS_REQUEST_ACTIVE = true

            val response = mWebService.getTerminals(ENDPOINTS.TERMINAL_LIST).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            val data = body?.response?.data
            mAppExecutors.mainThread {
                val sortedData = data?.sortedByDescending { it.terminalDataTimeLast?.millis }
                if (mPrefRepository.currentVehicle().isEmpty() && (sortedData?.size
                                ?: 0) > 0) {
                    mPrefRepository.changeCurrentVehicle(sortedData?.get(0)?.bstId,
                            sortedData?.get(0)?.vrn,
                            sortedData?.get(0)?.terminalID.toString(),
                    "${sortedData?.get(0)?.terminalDataLatitudeLast},${sortedData?.get(0)?.terminalDataLongitudeLast}")
                }
            }
            if (data?.size ?: 0 > 0) {
                mDao.refresh(data)
            }
            lastFetched = DateTime.now()
            IS_REQUEST_ACTIVE = false
            Status.SUCCESS
        } catch (ex: Exception) {
            IS_REQUEST_ACTIVE = false
            log(ex.message)
            Status.ERROR
        }
    }

    private fun doFetchUpdate(): Status {
        if (IS_REQUEST_ACTIVE)
            return Status.ONGOING

        return try {
            IS_REQUEST_ACTIVE = true

            val response = mWebService.getTerminals(ENDPOINTS.TERMINAL_LIST).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            val data = body?.response?.data

            /*mAppExecutors.mainThread {
                val sortedData = data?.sortedByDescending { it.terminalDataTimeLast?.millis }
                if (mPrefRepository.currentVehicle().isEmpty() && (sortedData?.size
                                ?: 0) > 0) {
                    mPrefRepository.changeCurrentVehicle(sortedData?.get(0)?.bstId,
                            sortedData?.get(0)?.vrn,
                            sortedData?.get(0)?.terminalID.toString())
                }
            }*/

            mDao.refresh(data)
            lastFetched = DateTime.now()
            IS_REQUEST_ACTIVE = false
            Status.SUCCESS
        } catch (ex: Exception) {
            IS_REQUEST_ACTIVE = false
            log(ex.message)
            Status.ERROR
        }
    }

    companion object {
        var IS_REQUEST_ACTIVE: Boolean = false
    }

    private fun shouldFetch(): Boolean {
        if (lastFetched == null)
            return true

        return lastFetched?.plusMinutes(1)?.isBeforeNow ?: true
    }

    override fun getVehicles(maxCount: Int): LiveData<List<Terminal>> {
        val output = mDao.terminal
//        fetch(false)
        return output
    }

    /*override fun getVehicleStatus(bstId: String): LiveData<Resource<VehicleStatus>> {
        Log.d("iukg","entered change vehicle 3")
        val output = MutableLiveData<Resource<VehicleStatus>>()
        output.postValue(Resource.loading(null))
        mAppExecutors.networkThread {
            val status = doFetch()

            if (status == Status.ONGOING)
                return@networkThread

            if (status == Status.ERROR) {
                output.postValue(Resource.error("Error", null))
                return@networkThread
            }
            val input = mDao.getTerminalByIdSync(bstId.removePrefix(Terminal.PREFIX))
            if (input != null) {
                Log.d("iukg","entered change vehicle 4")
                    mPrefRepository.changeCurrentVehicle(input?.bstId,
                            input.vrn,
                            input.terminalID.toString())

                val vehicleStatus = VehicleStatus()
                vehicleStatus.bid = input.bid.toIntOrNull() ?: 0
                vehicleStatus.bstid = input.bstid
                val locationResponse = LocationResponse()
                if (! input.isSuspended()) {
                    vehicleStatus.engineStatus = if (input.terminalDataIsAccOnLast == "1") "ON" else "OFF"
                    locationResponse.latitude = input.terminalDataLatitudeLast
                    locationResponse.longitude = input.terminalDataLongitudeLast
                    locationResponse.place = input.geoLocationName + " (" + input.geoLocationPositionLandmarkDistanceMeter + " m)"
                    vehicleStatus.speed = String.format("%.1f",
                            input.terminalDataVelocityLast?.toFloatOrNull())
                    vehicleStatus.updatedAt = input.terminalDataTimeLast?.toString(
                            "yyyy-MM-dd HH:mm:ss")
                } else {
                    vehicleStatus.updatedAt = ""
                    vehicleStatus.engineStatus = "--"
                    locationResponse.place = "--"
                    vehicleStatus.speed = "--"
                }
                vehicleStatus.location = locationResponse
                vehicleStatus.vrn = input.vrn
                output.postValue(Resource.success(vehicleStatus))
            } else {
                output.postValue(Resource.error<VehicleStatus>("Error", null))
            }
        }
        return output
    }*/

    override fun getVehicleStatus(bstId: String): LiveData<Resource<VehicleStatus>> {
        val output = MutableLiveData<Resource<VehicleStatus>>()
        output.postValue(Resource.loading(null))
        mAppExecutors.networkThread {
            val status = doFetchUpdate()

            if (status == Status.ONGOING)
                return@networkThread

            if (status == Status.ERROR) {
                output.postValue(Resource.error("Error", null))
                return@networkThread
            }
            val input = mDao.getTerminalByIdSync(bstId.removePrefix(Terminal.PREFIX))
            if (input != null) {


               val vehicleStatus = VehicleStatus()
                vehicleStatus.bid = input.bid.toIntOrNull() ?: 0
                vehicleStatus.bstid = input.bstid
                val locationResponse = LocationResponse()
                if (! input.isSuspended()) {

                    val distanceInMeter: Float = (input.geoLocationPositionLandmarkDistanceMeter?.toFloatOrNull()
                        ?: 0.0F)
                    var Distance_km=  String.format("%.1f KM", distanceInMeter / 1000)

                    vehicleStatus.engineStatus = if (input.terminalDataIsAccOnLast == "1") "ON" else "OFF"
                     locationResponse.latitude = input.terminalDataLatitudeLast
                     locationResponse.longitude = input.terminalDataLongitudeLast

                    locationResponse.place = input.geoLocationName + " (" + Distance_km + ")"

                   //locationResponse.place = input.geoLocationName + " (" + input.geoLocationPositionLandmarkDistanceMeter + "m)"
                    vehicleStatus.speed = String.format("%.1f",
                            input.terminalDataVelocityLast?.toFloatOrNull())
                    vehicleStatus.updatedAt = input.terminalDataTimeLast?.toString(
                            "yyyy-MM-dd HH:mm:ss")

                     Log.e("Distance_km:", "result: ${ locationResponse.place}")

                } else {

                    vehicleStatus.updatedAt = ""
                    vehicleStatus.engineStatus = "--"
                    locationResponse.place = "--"
                    vehicleStatus.speed = "--"

                }

                 vehicleStatus.location = locationResponse
                vehicleStatus.vrn = input.vrn

                output.postValue(Resource.success(vehicleStatus))

            } else {
                output.postValue(Resource.error<VehicleStatus>("Error", null))
            }
        }
        return output
    }

    override fun fetchVehicleRoutes(bstid: String,
                                    date: String): MutableLiveData<Resource<List<VehicleRoute>>> {
        val liveData = MutableLiveData<Resource<List<VehicleRoute>>>()
        mAppExecutors.networkThread {
            val giveDateTime = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"))
            val terminal = mDao.getById(bstid.removePrefix(Terminal.PREFIX))

            if (terminal?.isSuspended() == true) {
                liveData.postValue(Resource.error("Suspended Vehicle"))
                return@networkThread
            }

            val data = mRouteDao.getTerminalMinutelyDataByDateRangeAndId(
                    terminal?.terminalID?.toString() ?: "",
                    giveDateTime.withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                    giveDateTime.withTime(23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss")
            )

            liveData.postValue(Resource.loading(data.map { convertToVehicleRoute(it) }))

            val resource = getVehicleRoutes(terminal?.terminalID.toString(), giveDateTime)
            liveData.postValue(resource)
        }
        return liveData
    }

    override fun fetchCurrentRoutesVehicle(script: String, vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>> {
        val liveData = MutableLiveData<Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse>>()
        mAppExecutors.networkThread {
            val resource = fetchVehicleRoutes(vehicleRouteMode, terminalDataTimeFrom, terminalDataTimeTo, terminalId)
            liveData.postValue(resource)
        }
        return liveData
    }

    @WorkerThread
    private fun getTerminalMinutelyDataFromDb(bstId: String): List<VehicleRoute> {
        val data = mRouteDao.getTerminalMinutelyDataAsync(bstId.removePrefix(Terminal.PREFIX))
        val list = mutableListOf<VehicleRoute>()
        if (data.isNotEmpty()) {
            for (i in data) {
                val model = convertToVehicleRoute(i)
                list.add(model)
            }
        }
        return list
    }

    private fun convertToVehicleRoute(
            i: TerminalDataMinutely): VehicleRoute {
        val model = VehicleRoute()
        model.bstId = Terminal.PREFIX + i.terminalAssignmentCode
        model.engineStatus = if (i.terminalDataMinutelyIsAccOn == "0") "OFF" else if (i.terminalDataMinutelyIsAccOn == "1") "ON" else ""
        model.speed = String.format("%.1f",
                i.terminalDataMinutelyVelocityKmH?.toFloatOrNull() ?: 0f)
        val location = LocationResponse()
        location.latitude = i.terminalDataMinutelyLatitude
        location.longitude = i.terminalDataMinutelyLongitude
        location.place = i.geoLocationName ?: ""
        location.distance = String.format("%.1f",
                (i.geoLocationPositionLandmarkDistanceMeter?.toFloatOrNull() ?: 0F)) + "m)"
        model.location = location
        model.updatedAt = i.terminalDataMinutelyTimeLast ?: ""


        return model
    }

    @WorkerThread
    private fun getVehicleRoutes(terminalId: String, date: DateTime): Resource<List<VehicleRoute>> {
        try {
            val response = mWebService.getTerminalDataMinutely(
                    ENDPOINTS.TERMINAL_DATA_MINUTELY_SCRIPT,
                    date.withTime(0, 0, 0, 0).toString("yyyy-MM-dd HH:mm:ss"),
                    date.withTime(23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss"),
                    terminalId
            ).execute()
            response?.body()?.throwEventIfUnAuthenticated()
            if (response.isSuccessful && response.code() == 200) {
                val responseBody = response.body()?.response?.data
                responseBody?.let {
                    mRouteDao.save(it)
                }
                val list = mutableListOf<VehicleRoute>()
                if (responseBody != null) {
                    for (i in responseBody) {
                        val model = convertToVehicleRoute(i)
                        list.add(model)
                    }
                }
                return Resource.success(list)
            } else {
                val userMessage = parseErrorBody(response)
                return Resource.error(userMessage, null)
            }
        } catch (ex: Exception) {
            this.log(ex.message)
            FirebaseCrashlytics.getInstance().recordException(ex)
            if (networkAvailability.isNetworkAvailable()) {
                return Resource.error("No internet", null)
            }
            return Resource.error(parseErrorBody(), null)
        }
    }

    @WorkerThread
    private fun fetchVehicleRoutes(vehicleRouteMode: String, terminalDataTimeFrom: String, terminalDataTimeTo: String, terminalId: String): Resource<VehicleRouteTerminalData.VehicleRouteSuccessResponse> {
        try {
            val response = mWebService.fetchVehicleRoutesResponse(
                    ENDPOINTS.FETCH_ROUTE_VEHICLE,
                    vehicleRouteMode,
                    terminalDataTimeFrom,
                    terminalDataTimeTo,
                    terminalId

            )?.execute()

            if (response != null && response.isSuccessful && response.code() == 200) {
                val responseBody = response.body()?.vehiclesRouteSuccessResponse
                return Resource.success(responseBody)
            }
            else {
                val userMessage = parseErrorBody(response)
                return Resource.error(userMessage, null)
            }
        } catch (ex: Exception) {
            this.log(ex.message)
            FirebaseCrashlytics.getInstance().recordException(ex)
            if (networkAvailability.isNetworkAvailable()) {
                return Resource.error("No internet", null)
            }
            return Resource.error(parseErrorBody(), null)
        }
    }

    override fun getCurrentVehicle(completion: (Terminal?) -> Unit) {
        mAppExecutors.ioThread {
            val currentBst = mPrefRepository.currentVehicle()
            val terminal = mDao.getById(currentBst?.removePrefix(Terminal.PREFIX))
            terminal?.let {
                mAppExecutors.mainThread { completion(terminal) }
            }
        }
    }

    override fun getVeicleRoutePolyLineAsync(bstId: String, date: DateTime,
                                             completion: (VehicleRoutePolyline?) -> Unit) {
        mAppExecutors.ioThread {
            val output = getVeicleRoutePolyLine(bstId, date)
            completion(output)
        }
    }

    override fun getVeicleRoutePolyLine(bstId: String, date: DateTime): VehicleRoutePolyline? {

        val terminal = mDao.getById(bstId.removePrefix(Terminal.PREFIX)) ?: return null

        val data = mRouteDao.getTerminalMinutelyDataByDateRangeAndId(
                terminal?.terminalID?.toString() ?: "",
                date.withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                date.withTime(23, 59, 59, 999).toString("yyyy-MM-dd HH:mm:ss")
        )

        val points = mutableListOf<Point>()

        data.sortedBy { it.terminalDataMinutelyTimeFirst }.forEach {
            val ps = it.terminalDataMinutelyCoordinateList?.split(";") ?: listOf()
            if (ps.isNotEmpty()) {
                ps.forEach { p ->
                    val outputs = p.split(",")
                    if (outputs.size == 2) {
                        val lat = outputs[0].toDoubleOrNull()
                        val lon = outputs[1].toDoubleOrNull()
                        if (lat != null && lon != null) {
                            points.add(Point(lat, lon))
                        }
                    }
                }
            } else {
                val lat = it.terminalDataMinutelyLatitude?.toDoubleOrNull()
                val lon = it.terminalDataMinutelyLongitude?.toDoubleOrNull()
                if (lat != null && lon != null) {
                    points.add(Point(lat, lon))
                }
            }
        }

        val output = VehicleRoutePolyline()
        output.bstId = bstId
        output.date = date.toString("yyyy-MM-dd")
        output.polyline = ""
        output.latlagns = points

        return output
    }


    @WorkerThread
    fun fetchTerminalAggregatedData(date: DateTime,
                                    bId: String? = null): Resource<List<TerminalAggregatedData>> {
        try {
            val response =
                    if (bId == null) {
                        val terminalAggregatedData = mWebService.getTerminalAggregatedData(
                                ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
                                date.withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                                date.plusDays(1).withTimeAtStartOfDay().minusSeconds(1).toString(
                                        "yyyy-MM-dd HH:mm:ss"))
                        terminalAggregatedData.execute()
                    } else mWebService.getTerminalAggregatedDataByTerminalId(
                            ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
                            date.withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"),
                            date.plusDays(1).withTimeAtStartOfDay().minusSeconds(1).toString(
                                    "yyyy-MM-dd HH:mm:ss"), bId).execute()
            val body = response.body()
            body?.throwEventIfUnAuthenticated()
            return if (body?.isFailed() == false) {
                // Successfully fetched
                val data = body.response?.data
                data?.forEach {
                    it.aggregatedDurationStartFrom = date.withTimeAtStartOfDay()
                    it.aggregatedDurationType = "daily"
                }
                Resource.success(data)
            } else {
                Resource.error(body?.error?.description ?: "Something went wrong", null)
            }
        } catch (ex: Exception) {

            return Resource.error(ex.message ?: "Something went wrong", null)

        }
    }

    override fun getTodaysTravelledDistance(bId: String): LiveData<TerminalAggregatedData> {
        return mTerminalAggregatedDataDao.getTerminalAggregatedDataByDayTerminal(
                DateTime.now().withTimeAtStartOfDay().millis, "daily", bId)
    }

    override fun fetchTodaysTravelledDistance(bId: String) {
        fetchAndSaveTerminalAggregatedData(DateTime.now(), bId)
    }

    private fun fetchAndSaveTerminalAggregatedData(date: DateTime, bId: String? = null) {
        mAppExecutors.ioThread {
            val output = fetchTerminalAggregatedData(date, bId)
            if (output.status == Status.SUCCESS) {
                output.data?.let { mTerminalAggregatedDataDao.save(it) }
            }
        }

    }

    override fun getTodaysTravelledAllDistance(): LiveData<List<TerminalAggregatedData>> {
        return mTerminalAggregatedDataDao.getTerminalAggregatedAllDataByRangeTerminal(
                DateTime.now().withTimeAtStartOfDay().millis,
                DateTime.now().withTimeAtStartOfDay().plusDays(1).millis,
                "daily"
        )
    }

    override fun selectNextVehicle(direction: Int) {
        val current = mPrefRepository.currentVehicle()
        mAppExecutors.ioThread {
            val terminal = mDao.allTerminal
            var index = terminal.indexOfFirst { it.bstId == current }
            if (index >= 0) {
                index = if (direction == 0) {
                    (index + 1)
                } else {
                    (index - 1 + terminal.size)
                } % terminal.size
                val newVehicle = terminal.get(index)
                mAppExecutors.mainThread {
                    mPrefRepository.changeCurrentVehicle(
                            newVehicle.bstId,
                            newVehicle.vrn,
                            newVehicle.bid,
                            "${newVehicle.terminalDataLatitudeLast},${newVehicle.terminalDataLongitudeLast}"
                    )
                }
            }
        }
    }
}