package com.singularity.trackmyvehicle.view.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.VirtualDeactivationModel
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.utils.Log
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.map.MapDrawer
import kotlinx.android.synthetic.main.activity_help_and_support.*
import kotlinx.android.synthetic.main.activity_virtual_watchman_activation.*
import kotlinx.android.synthetic.main.activity_virtual_watchman_set.*
import kotlinx.android.synthetic.main.layout_virtual_watchman_activation.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class VirtualWatchmanActivation : AppCompatActivity() {

    private var mDialog: MaterialDialog? = null

    var terminalId = 0

    @Inject
    lateinit var mPrefRepository: PrefRepository

    @Inject
    lateinit var mVehicleRepository: VehicleRepository

    private var mMapDrawer: MapDrawer? = null

    var cookie : String = ""
    var convertParkingTimeExpiry : String = ""
    var serverDateTime : String = ""

    var selectedVehicleLatitude : Double = 0.00
    var selectedVehicleLongitude : Double = 0.00

    var updateRadiusMeter = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_virtual_watchman_activation)
        setContentView(R.layout.layout_virtual_watchman_activation)
        setSupportActionBar(toolbarActivation)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val radiusMeterOnlyFirstTime = intent?.getIntExtra("radiusMeterOnlyFirstTime", 0)
        val radiusMeter = intent?.getStringExtra("radiusMeter")

        terminalId = intent.getIntExtra("terminalId", 0)

        cookie = intent.getStringExtra("cookie").toString()

        convertParkingTimeExpiry = intent.getStringExtra("ParkingTimeExpiry").toString()
        serverDateTime = intent.getStringExtra("serverDateTime").toString()


        selectedVehicleLatitude = intent.getDoubleExtra("selectedVehicleLatitude", 0.00)
        selectedVehicleLongitude = intent.getDoubleExtra("selectedVehicleLongitude", 0.00)


        Log.d("kjbgik","$convertParkingTimeExpiry")
        Log.d("kjbgik","$serverDateTime")

        /*if(selectedMinute == 0){
            //selectedTime_textView.text = minutes.toString() ?: ""
            textView_timeRemaining.text = timeString.toString() ?: ""

        }else{
            //selectedTime_textView.text = Integer.toString(selectedMinute)
            textView_timeRemaining.text = Integer.toString(selectedMinute)

        }*/

        if(radiusMeterOnlyFirstTime == 0){
            textView_meter.text = "${radiusMeter?.toDouble()?.toInt()} m"
            updateRadiusMeter = radiusMeter?.toDouble() !!
        }else{
            textView_meter.text = "$radiusMeterOnlyFirstTime m"
            updateRadiusMeter = radiusMeterOnlyFirstTime?.toDouble() !!
        }


        /*deactivated_button.setOnClickListener {
            deactivateVirtualActivation()
        }*/

        textView_deactivate.setOnClickListener {
            deactivateVirtualActivation()
        }

        backPressed()
        
        setTimeRemaining()

        setSelectedVehicleCurrentLocation()

    }

    private fun setSelectedVehicleCurrentLocation() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (isGooglePlayServicesAvailable(this)) {
            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
            mapFragment.getMapAsync(mMapDrawer)
        }

        //mMapDrawer?.placeSelectedVehicleCurrentLocationOnMap(true, selectedVehicleLatitude, selectedVehicleLongitude)
        mMapDrawer?.addSelectedRadiusCircle(true, LatLng(selectedVehicleLatitude, selectedVehicleLongitude), updateRadiusMeter)

    }

    private lateinit var countDownTimer:CountDownTimer

    fun setTimeRemaining() {

        val currentTime = Calendar.getInstance().time
        //val endDateDay = "25/01/2022 01:17:53"
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val endDate = format.parse(convertParkingTimeExpiry)
        val serverDate = format.parse(serverDateTime)

        //milliseconds
        var different = endDate.time - serverDate.time
        countDownTimer = object : CountDownTimer(different, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays = diff / daysInMilli
                diff %= daysInMilli

                val elapsedHours = diff / hoursInMilli
                diff %= hoursInMilli

                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli

                val elapsedSeconds = diff / secondsInMilli

                chronometer_time.text = "$elapsedHours : $elapsedMinutes : $elapsedSeconds"
            }

            override fun onFinish() {
                chronometer_time.text = "done!"
            }
        }.start()
    }

    private fun backPressed() {
        imageView_arrow.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            return when (it.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> {
                    false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deactivateVirtualActivation() {
        showLoading()

        val call = RetrofitClient.getInstance(cookie)
                ?.create(ApiHandler::class.java)
                ?.deactivateVirtualActivation(
                        "API/V1/Parking/Cancel",
                        terminalId
                )
        call?.enqueue(object: Callback<VirtualDeactivationModel>{
            override fun onFailure(call: Call<VirtualDeactivationModel>, t: Throwable) {
                if (! this@VirtualWatchmanActivation.isDestroyed) dismissDialog()
                showDialogError("Something went wrong, Please try again")
            }

            override fun onResponse(call: Call<VirtualDeactivationModel>, response: Response<VirtualDeactivationModel>) {
                if (! this@VirtualWatchmanActivation.isDestroyed) dismissDialog()
                if (response.isSuccessful && response.body() != null && response.body()?.error?.code == 0) {
                    val virtualWatchmandeactivationModel: VirtualDeactivationModel? = response.body()

                    Log.d("response",""+virtualWatchmandeactivationModel?.response?.status)
                    Log.d("response",""+virtualWatchmandeactivationModel?.error?.code)
                    Log.d("response",""+virtualWatchmandeactivationModel?.error?.description)

                    Toast.makeText(this@VirtualWatchmanActivation,"Virtual Watchman Cancelled Successfully", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }
        })


    }

    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance();
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404)?.show();
            }
            return false;
        }
        return true;
    }

    private fun showLoading() {
        dismissDialog()
        mDialog = DialogHelper.getLoadingDailog(this, getString(R.string.msg_hold_on),
                getString(R.string.msg_loading))
                ?.show()
    }

    private fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }

    private fun showDialogError(msg: String) {
        dismissDialog()
        mDialog = DialogHelper.getMessageDialog(this, getString(R.string.title_error), msg)
                ?.positiveText(getString(R.string.action_ok))
                ?.show()
    }
}
