package com.singularity.trackmyvehicle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.apiResponse.v2.ErrorResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatterBuilder
import retrofit2.Response
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Sadman Sarar on 3/13/18.
 */

fun parseErrorBody(response: Response<out Any>? = null): String {
    var userMsg = "Something went wrong"
    try {
        val error = Gson().fromJson(response?.errorBody()?.string(), ErrorResponse::class.java)
        userMsg = error.userMessage

    } catch (ex: Exception) {
        FirebaseCrashlytics.getInstance().recordException(ex)
        ex.printStackTrace()
    }
    return userMsg
}


fun getAngle(source: LatLng, destination: LatLng): Float {

    var theta = Math.atan2(
            destination.longitude - source.longitude, destination.latitude - source.latitude)

    theta += Math.PI / 2.0

    var angle = Math.toDegrees(theta)

    if (angle < 0) {
        angle += 360.0
    }

    return angle.toFloat()
}

fun enableFCM(mFCMRepository: FCMRepository, appPreference: AppPreference, mPrefRepository: PrefRepository) {
    Log.d("kgjk","enter enable FCM")
    // Enable FCM via enable Auto-init service which generate new token and receive in FCMService
    FirebaseMessaging.getInstance().isAutoInitEnabled = true
    if (mFCMRepository.shouldSendFCMToken() && appPreference.getBoolean(AppPreference.isNotificationEnable)) {
        mFCMRepository.postToken(mPrefRepository.unsentFCMToken())
    }
}

fun disableFCM() {
    Log.d("kgjk","enter disable FCM")
    // Disable auto init
    FirebaseMessaging.getInstance().isAutoInitEnabled = false
    Thread {
        try {
            Log.d("kgjk","enter disable FCM 1")
            // Remove InstanceID initiate to unsubscribe all topic
            // TODO: May be a better way to use FirebaseMessaging.getInstance().unsubscribeFromTopic()
            FirebaseInstanceId.getInstance().deleteInstanceId().let {

                Log.d("kgjk","enter disable FCM qqq d0ne")
            }
        } catch (e: IOException) {
            Log.d("kgjk","enter disable FCM 2 $e")
            e.printStackTrace()
        }
    }.start()
}

fun bitmapSizeByScall(bitmapIn: Bitmap, scall_zero_to_one_f: Float): Bitmap {
    return Bitmap.createScaledBitmap(bitmapIn,
            Math.round(bitmapIn.width * scall_zero_to_one_f),
            Math.round(bitmapIn.height * scall_zero_to_one_f), false)
}

fun getRelativeTimeFromNow(dateTime: DateTime): String? {
    val now = DateTime.now()
    val period = Period(dateTime, now)


    val formatBuilder = PeriodFormatterBuilder()

    if (period.hours > 0) {
        formatBuilder
                .appendYears().appendSuffix(" years").appendSeparator(" ")
                .appendMonths().appendSuffix(" months").appendSeparator(" ")
                .appendWeeks().appendSuffix(" weeks").appendSeparator(" ")
                .appendDays().appendSuffix(" days").appendSeparator(" ")
                .appendHours().appendSuffix(" hours").appendSeparator(" ")
    } else {
        formatBuilder
                .appendMinutes().appendSuffix(" minutes").appendSeparator(" ")
                .appendSeconds().appendSuffix(" seconds").appendSeparator(" ")
    }

    var formatter = formatBuilder
            .printZeroNever()
            .toFormatter()

    return formatter.print(period) + " ago"
}

class TimestampConverter {
    companion object {
        val df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    }
}

fun getTimeDuration(minutes: Int): String? {
    val now = DateTime.now()
    val period = Period(now.minusSeconds(minutes), now)


    val formatBuilder = PeriodFormatterBuilder()

    if (period.hours > 0) {
        formatBuilder
                .appendYears().appendSuffix(" years").appendSeparator(" ")
                .appendMonths().appendSuffix(" months").appendSeparator(" ")
                .appendWeeks().appendSuffix(" weeks").appendSeparator(" ")
                .appendDays().appendSuffix(" days").appendSeparator(" ")
                .appendHours().appendSuffix(" hours").appendSeparator(" ")
    } else {
        formatBuilder
                .appendMinutes().appendSuffix(" minutes").appendSeparator(" ")
    }

    var formatter = formatBuilder
            .printZeroNever()
            .toFormatter()

    return formatter.print(period)
}

fun convertVehicleStatus(terminal: Terminal): VehicleStatus {
    val vehicleStatus = VehicleStatus()
    vehicleStatus.bid = terminal.bid.toIntOrNull() ?: 0
    vehicleStatus.bstid = terminal.bstid

    val locationResponse = LocationResponse()
    if (!terminal.isSuspended()) {

        val distanceInMeter: Float = (terminal.geoLocationPositionLandmarkDistanceMeter?.toFloatOrNull()
            ?: 0.0F)

        var Distance_km=  String.format("%.1f KM", distanceInMeter / 1000)

        vehicleStatus.engineStatus = if (terminal.terminalDataIsAccOnLast == "1") "ON" else "OFF"
        locationResponse.latitude = terminal.terminalDataLatitudeLast
        locationResponse.longitude = terminal.terminalDataLongitudeLast
       // locationResponse.place = terminal.geoLocationName + " (" + terminal.geoLocationPositionLandmarkDistanceMeter + " km)"
        locationResponse.place = terminal.geoLocationName + " (" + Distance_km + ")"
        vehicleStatus.speed = String.format("%.1f", terminal.terminalDataVelocityLast?.toFloatOrNull())
        vehicleStatus.updatedAt = terminal.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")

    } else {
        vehicleStatus.updatedAt = ""
        vehicleStatus.engineStatus = "--"
        locationResponse.place = "--"
        vehicleStatus.speed = "--"
    }
    vehicleStatus.location = locationResponse
    vehicleStatus.vrn = terminal.vrn

    return vehicleStatus
}

val ALL_VEHICLES_TYPE = 0
val MOVING_VEHICLES_TYPE = 1
val IDLE_VEHICLES_TYPE = 2
val ENGINE_OFF_VEHICLES_TYPE = 3
val OFFLINE_VEHICLES_TYPE = 4
val SUSPENDED_VEHICLES_TYPE = 5

fun setSelectedVehicleStatusColor(context: Context, vehicleType: Int): Int {
    if(vehicleType == ALL_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.colorPrimary)

    }else if(vehicleType == MOVING_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.moving)

    }else if(vehicleType == IDLE_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.idle)

    }else if(vehicleType == ENGINE_OFF_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.engineOff)

    }else if(vehicleType == OFFLINE_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.offline)

    }else if(vehicleType == SUSPENDED_VEHICLES_TYPE){
       return ContextCompat.getColor(context, R.color.suspended)
    }

    return ContextCompat.getColor(context, R.color.colorPrimary)
}

val VEHICLE_UPDATE_TYPE = 0
val AFTER_LOGIN_VEHICLE_UPDATE_TYPE = 1

fun setDisableMarker(context: Context, carrierTypeName: String) : Bitmap{

    lateinit var carIcon : Bitmap

    when(carrierTypeName){

        context.getString(R.string.ambulance) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ambulance_disable_marker)
        context.getString(R.string.barge) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_barge_disable_marker)
        context.getString(R.string.bike) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bike_disable_marker)
        context.getString(R.string.bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_disable_marker)
        context.getString(R.string.car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car_disable_marker)
        context.getString(R.string.cng) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_disable_marker)
        context.getString(R.string.wheelerlpg) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_disable_marker)
        context.getString(R.string.launch) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launch_disable_marker)
        context.getString(R.string.lorry) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_lorry_disable_marker)
        context.getString(R.string.micro_bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_disable_marker)
        context.getString(R.string.micro_bus_two) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_disable_marker)
        context.getString(R.string.pick_up) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_disable_marker)
        context.getString(R.string.pick_up2) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_disable_marker)
        context.getString(R.string.road_tanker) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_road_tanker_disable_marker)
        context.getString(R.string.ship) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ship_disable_marker)
        context.getString(R.string.suv) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suv_disable_marker)
        context.getString(R.string.van) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_van_disable_marker)
        context.getString(R.string.default_car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_disable_marker)

        else -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_unknown_disable_marker)

    }

    return carIcon
}

fun setIdleMarker(context: Context, carrierTypeName: String) : Bitmap{

    lateinit var carIcon : Bitmap

    when(carrierTypeName){

        context.getString(R.string.ambulance) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ambulance_idle_marker)
        context.getString(R.string.barge) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_barge_idle_marker)
        context.getString(R.string.bike) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bike_idle_marker)
        context.getString(R.string.bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_idle_marker)
        context.getString(R.string.car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car_idle_marker)
        context.getString(R.string.cng) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_idle_marker)
        context.getString(R.string.wheelerlpg) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_idle_marker)
        context.getString(R.string.launch) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launch_idle_marker)
        context.getString(R.string.lorry) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_lorry_idle_marker)
        context.getString(R.string.micro_bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_idle_marker)
        context.getString(R.string.micro_bus_two) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_idle_marker)
        context.getString(R.string.pick_up) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_idle_marker)
        context.getString(R.string.pick_up2) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_idle_marker)
        context.getString(R.string.road_tanker) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_road_tanker_idle_marker)
        context.getString(R.string.ship) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ship_idle_marker)
        context.getString(R.string.suv) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suv_idle_marker)
        context.getString(R.string.van) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_van_idle_marker)
        context.getString(R.string.default_car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_idle_marker)

        else -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_unknown_idle_marker)

    }

    return carIcon
}

fun setMovingMarker(context: Context, carrierTypeName: String) : Bitmap{

    lateinit var carIcon : Bitmap

    when(carrierTypeName){

        context.getString(R.string.ambulance) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ambulance_moving_marker)
        context.getString(R.string.barge) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_barge_moving_marker)
        context.getString(R.string.bike) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bike_moving_marker)
        context.getString(R.string.bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_moving_marker)
        context.getString(R.string.car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car_moving_marker)
        context.getString(R.string.cng) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_moving_marker)
        context.getString(R.string.wheelerlpg) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_moving_marker)
        context.getString(R.string.launch) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launch_moving_marker)
        context.getString(R.string.lorry) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_lorry_moving_marker)
        context.getString(R.string.micro_bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_moving_marker)
        context.getString(R.string.micro_bus_two) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_moving_marker)
        context.getString(R.string.pick_up) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_moving_marker)
        context.getString(R.string.pick_up2) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_moving_marker)
        context.getString(R.string.road_tanker) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_road_tanker_moving_marker)
        context.getString(R.string.ship) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ship_moving_marker)
        context.getString(R.string.suv) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suv_moving_marker)
        context.getString(R.string.van) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_van_moving_marker)
        context.getString(R.string.default_car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_moving_marker)

        else -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_unknown_moving_marker)

    }

    return carIcon
}

fun setEngineOffMarker(context: Context, carrierTypeName: String) : Bitmap{

    lateinit var carIcon : Bitmap

    when(carrierTypeName){

        context.getString(R.string.ambulance) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ambulance_engine_off_marker)
        context.getString(R.string.barge) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_barge_engine_off_marker)
        context.getString(R.string.bike) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bike_engine_off_marker)
        context.getString(R.string.bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_engine_off_marker)
        context.getString(R.string.car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car_engine_off_marker)
        context.getString(R.string.cng) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_engine_off_marker)
        context.getString(R.string.wheelerlpg) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_engine_off_marker)
        context.getString(R.string.launch) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launch_engine_off_marker)
        context.getString(R.string.lorry) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_lorry_engine_off_marker)
        context.getString(R.string.micro_bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_engine_off_marker)
         context.getString(R.string.micro_bus_two) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_engine_off_marker)
        context.getString(R.string.pick_up) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_engine_off_marker)
        context.getString(R.string.pick_up2) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_engine_off_marker)
        context.getString(R.string.road_tanker) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_road_tanker_engine_off_marker)
        context.getString(R.string.ship) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ship_engine_off_marker)
        context.getString(R.string.suv) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suv_engine_off_marker)
        context.getString(R.string.van) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_van_engine_off_marker)
        context.getString(R.string.default_car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_engine_off_marker)

        else -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_unknown_engine_off_marker)

    }

    return carIcon
}

fun setRedMarker(context: Context, carrierTypeName: String) : Bitmap{

    lateinit var carIcon : Bitmap

    when(carrierTypeName){

        context.getString(R.string.ambulance) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ambulance_red_marker)
        context.getString(R.string.barge) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_barge_red_marker)
        context.getString(R.string.bike) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bike_red_marker)
        context.getString(R.string.bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_red_marker)
        context.getString(R.string.car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car_red_marker)
        context.getString(R.string.cng) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_cng_red_marker)
        context.getString(R.string.launch) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launch_red_marker)
        context.getString(R.string.lorry) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_lorry_red_marker)
        context.getString(R.string.micro_bus) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_micro_bus_red_marker)
        context.getString(R.string.pick_up) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_red_marker)
        context.getString(R.string.pick_up2) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pick_up_red_marker)
        context.getString(R.string.road_tanker) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_road_tanker_red_marker)
        context.getString(R.string.ship) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ship_red_marker)
        context.getString(R.string.suv) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suv_red_marker)
        context.getString(R.string.van) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_van_red_marker)
        context.getString(R.string.default_car) -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_red_marker)

        else -> carIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_unknown_red_marker)

    }

    return carIcon
}

fun customizeDateTimeFormat(dateTime: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var convertedDate: Date? = null
    var formattedDate: String = ""
    try {
        convertedDate = sdf.parse(dateTime)
        formattedDate = SimpleDateFormat("MMM dd, yyyy hh:mm:ss").format(convertedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return formattedDate
}

const val VEHICLE_MOVING = 1
const val VEHICLE_IDLE = 2
const val VEHICLE_ENGINE_OFF = 3
const val VEHICLE_OFFLINE = 4
const val VEHICLE_SUSPENDED = 5

fun getVehicleState(terminal: Terminal): Int {
    val lastUpdatedDate = terminal.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")

    val updatedDate = try {
        DateTime.parse(lastUpdatedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    } catch (ex: Exception) {
        null
    }

    if (terminal.isSuspended()) {
        return VEHICLE_SUSPENDED

    } else if (updatedDate?.isBefore(DateTime.now().minusDays(1)) == true) {
        return VEHICLE_OFFLINE

    } else if (terminal.terminalDataIsAccOnLast == "1" && (terminal.terminalDataVelocityLast?.toDouble() ?: 0.0) == 0.0) {
        return VEHICLE_IDLE

    } else if (terminal.terminalDataIsAccOnLast == "1") {
        return VEHICLE_MOVING
    }

    return VEHICLE_ENGINE_OFF
}

