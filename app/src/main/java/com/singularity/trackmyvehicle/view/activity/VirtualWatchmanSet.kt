package com.singularity.trackmyvehicle.view.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v3.VirtualActivationParkingListModel
import com.singularity.trackmyvehicle.model.apiResponse.v3.VirtualActivationParkingListQueryParam
import com.singularity.trackmyvehicle.model.apiResponse.v3.VirtualWatchmanActivationModel
import com.singularity.trackmyvehicle.model.apiResponse.v3.VirtualWatchmanActivationPostBody
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.map.MapDrawer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_virtual_watchman_set.*
import kotlinx.android.synthetic.main.layout_current_vehicle_2.*
import kotlinx.android.synthetic.main.layout_virtual_watchman.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class VirtualWatchmanSet : BaseActivity() {

    var data: Array<String>? = null
    var terminalId: String? = ""
    var selectedVehicleLatitude: Double = 0.00
    var selectedVehicleLongitude: Double = 0.00
    var pos: Int? = null
    var selectPicker: String? = null
    var selectedMinute: Int = 0
    var radiusMeter : Int = 50
    var selectMinute : Int = 0

    private var mMapDrawer: MapDrawer? = null

    @Inject
    lateinit var mVehicleRepository: VehicleRepository

    @Inject
    lateinit var mPrefRepository: PrefRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mDialog: MaterialDialog? = null

    val queryVirtualActivationList = VirtualActivationParkingListQueryParam()

    var isVirtualWatchman = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_virtual_watchman_set)
        setContentView(R.layout.layout_virtual_watchman)
        setSupportActionBar(toolbarSet)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terminalId = intent.getStringExtra("Terminal_ID")
        selectedVehicleLatitude = intent.getDoubleExtra("Selected_Vehicle_Latitude", 0.00)
        selectedVehicleLongitude = intent.getDoubleExtra("Selected_Vehicle_Longitude", 0.00)

        queryVirtualActivationList.ParkingIsActive = 1 ?: 0
        queryVirtualActivationList.script = "API/V1/Parking/List" ?: ""
        queryVirtualActivationList.terminalID = terminalId.toString().toInt() ?: 0

        if (validateInput()) {
            getVirtualActivationList()
        }

        /*data = arrayOf("20m", "50m", "100m", "250m", "500m", "1000m")
        number_picker.minValue = 0
        number_picker.maxValue = data !!.size - 1
        number_picker.displayedValues = data

        selectedMinute = 30
        fixedminute_textView.text = Integer.toString(selectedMinute)

        decreaseValue.setOnClickListener {
            if(selectedMinute > 0 ){
                selectedMinute -= 5
            }


            fixedminute_textView.text = String.format("%d", selectedMinute)

        }

        increaseValue.setOnClickListener {

            if(selectedMinute <= 43200){
                selectedMinute += 5
            }

            fixedminute_textView.text = String.format("%d", selectedMinute)

        }

        activated_button.setOnClickListener {
            postVirtualActivation()

        }*/

        textView_activate.setOnClickListener {
            postVirtualActivation()
        }
        
        getSeekbarProgress()

        getSelectedMinute()

        backPressed()

        setSelectedVehicleCurrentLocation()
    }

    private fun setSelectedVehicleCurrentLocation() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (isGooglePlayServicesAvailable(this)) {
            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
            mapFragment.getMapAsync(mMapDrawer)
        }

        //mMapDrawer?.placeSelectedVehicleCurrentLocationOnMap(true, selectedVehicleLatitude, selectedVehicleLongitude)
        mMapDrawer?.addSelectedRadiusCircle(true, LatLng(selectedVehicleLatitude, selectedVehicleLongitude), radiusMeter.toDouble())

    }

    private fun backPressed() {
        imageView_arrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getSelectedMinute() {
        layout_ten_minute.setOnClickListener{
            layout_ten_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_fill_color_primary)

            layout_thirty_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_one_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_two_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_six_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)

            selectMinute = 10
        }
        layout_thirty_minute.setOnClickListener{
            layout_thirty_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_fill_color_primary)

            layout_one_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_two_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_six_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_ten_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)

            selectMinute = 30
        }
        layout_one_hour.setOnClickListener {
            layout_one_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_fill_color_primary)

            layout_two_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_six_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_ten_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_thirty_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)

            selectMinute = 60
        }
        layout_two_hour.setOnClickListener {
            layout_two_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_fill_color_primary)

            layout_six_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_ten_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_thirty_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_one_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)

            selectMinute = 120
        }
        layout_six_hour.setOnClickListener {
            layout_six_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_fill_color_primary)

            layout_ten_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_thirty_minute.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_one_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)
            layout_two_hour.background = ContextCompat.getDrawable(this, R.drawable.bg_rectangle_border_color_primary)

            selectMinute = 360
        }

    }

    private fun getSeekbarProgress() {

        textView_meter.text = "$radiusMeter m"


        seekbar_radius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("kkvj","$progress")
                radiusMeter = progress

                textView_meter.text = "$radiusMeter m"

                mMapDrawer?.addSelectedRadiusCircle(true, LatLng(selectedVehicleLatitude, selectedVehicleLongitude), radiusMeter.toDouble())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {


            }

        })
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

    private fun validateInput(): Boolean {

        if (queryVirtualActivationList.ParkingIsActive == 0) {
            Toasty.error(this@VirtualWatchmanSet, "Parking Status is not valid", Toast.LENGTH_SHORT,
                    true).show()
            return false
        }
        if (queryVirtualActivationList.terminalID == 0) {
            Toasty.error(this@VirtualWatchmanSet, "Terminal id is not valid", Toast.LENGTH_SHORT,
                    true).show()
            return false
        }
        if (queryVirtualActivationList.script.isEmpty()) {
            Toasty.error(this@VirtualWatchmanSet, "Script is empty", Toast.LENGTH_SHORT,
                    true).show()
            return false
        }
        return true
    }

    private fun getVirtualActivationList() {
        showLoading()

        val call = RetrofitClient.getInstance(mPrefRepository.cookie)
                ?.create(ApiHandler::class.java)?.getParkingList(
                        queryVirtualActivationList.script,
                        queryVirtualActivationList.terminalID,
                        queryVirtualActivationList.ParkingIsActive
                )

        call?.enqueue(object : Callback<VirtualActivationParkingListModel> {
            override fun onFailure(call: Call<VirtualActivationParkingListModel>, t: Throwable) {
                if (! this@VirtualWatchmanSet.isDestroyed) dismissDialog()
                showDialogError("Something went wrong, Please try again")
            }

            override fun onResponse(call: Call<VirtualActivationParkingListModel>, response: Response<VirtualActivationParkingListModel>) {
                if (! this@VirtualWatchmanSet.isDestroyed) dismissDialog()
                if (response.isSuccessful && response.body() != null && response.body()?.error?.code == 0) {
                    //Log.d("parkingTimeExpiry","Enter in Response")

                    if(response?.body()?.response?.parking.isNullOrEmpty()){
                        Log.d("prt","parking list is null"+ queryVirtualActivationList.terminalID)

                    }else if(response?.body()?.response?.parking?.isNotEmpty() !!){

                        val virtualActivationParkingListModel: VirtualActivationParkingListModel? = response.body()

                        var serverTime = virtualActivationParkingListModel?.response?.server?.time.toString()
                        var parkingTimeExpiry = virtualActivationParkingListModel?.response?.parking?.get(0)?.parkingTimeExpiry.toString()
                        var radiusMeter = virtualActivationParkingListModel?.response?.parking?.get(0)?.parkingRadiusMeter

                        /* date time */
                        Log.d("serverTime",""+serverTime)
                        Log.d("parkingTimeExpiry",""+parkingTimeExpiry)

                        var expireDateTime : String = convertDateTimeFormate(parkingTimeExpiry)
                        var serverDateTime : String = convertDateTimeFormate(serverTime)

                        val convertServerTime = SimpleDateFormat("yyyy-M-dd hh:mm:ss").parse(serverTime)
                        val convertParkingTimeExpiry = SimpleDateFormat("yyyy-M-dd hh:mm:ss").parse(parkingTimeExpiry)

                        val diff =  convertParkingTimeExpiry.time - convertServerTime.time
                        val numOfDays = (diff / (1000 * 60 * 60 * 24)).toInt()
                        val hours = (diff / (1000 * 60 * 60)).toInt()
                        val minutes = (diff / (1000 * 60)).toInt()
                        val seconds = (diff / 1000).toInt()

                        Log.d("svcont",""+diff)
                        Log.d("svcont",""+hours)
                        Log.d("svcont",""+minutes)
                        Log.d("svcont",""+seconds)
                        Log.d("svcont",""+convertParkingTimeExpiry)
                        Log.d("svcont",""+convertServerTime)
                        Log.d("svcont",""+expireDateTime)

                        var timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                        val intent = Intent(this@VirtualWatchmanSet, VirtualWatchmanActivation ::class.java)
                        intent.putExtra("cookie", mPrefRepository.cookie)
                        intent.putExtra("radiusMeter", radiusMeter)
                        intent.putExtra("ParkingTimeExpiry", expireDateTime)
                        intent.putExtra("serverDateTime", serverDateTime)
                        intent.putExtra("selectedVehicleLatitude", selectedVehicleLatitude)
                        intent.putExtra("selectedVehicleLongitude", selectedVehicleLongitude)
                        startActivity(intent)
                        finish()

                    }
                }
            }

        })

    }

    fun convertDateTimeFormate(dateTime: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        var convertedDate: Date? = null
        var formattedDate: String = ""
        try {
            convertedDate = sdf.parse(dateTime)
            formattedDate = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return formattedDate
    }


    private fun postVirtualActivation() {
        getVirtualActivationLiveData()
    }


    private fun getVirtualActivationLiveData() {

        //pos = number_picker.value
        //selectPicker = data !!.get(pos !!)
        //selectPicker = selectPicker?.removeSuffix("m")
        //Log.v("taggg", "Click on current value" + selectPicker)

        val body = VirtualWatchmanActivationPostBody()
        //body.parkingDurationMinute = selectedMinute
        body.parkingDurationMinute = selectMinute
        //body.parkingRadiusMeter = selectPicker?.toInt() ?: 0
        body.parkingRadiusMeter = radiusMeter
        body.terminalID = terminalId?.toInt() ?: 0
        body.script = "API/V1/Parking/Create"

        Log.d("iyiliy","${Gson().toJson(body)}")

        if(body.parkingDurationMinute == 0){
            Toast.makeText(this,"Parking duration is 0",Toast.LENGTH_SHORT).show()
        }else{
            showLoading()

            Log.d("dscsd", "cookie: " + mPrefRepository.cookie)

            val call = RetrofitClient.getInstance(mPrefRepository.cookie)
                    ?.create(ApiHandler::class.java)
                    ?.createVirtualActivation(
                            body.script,
                            body.terminalID,
                            body.parkingDurationMinute,
                            body.parkingRadiusMeter
                    )

            call?.enqueue(object : Callback<VirtualWatchmanActivationModel> {
                override fun onFailure(call: Call<VirtualWatchmanActivationModel>, t: Throwable) {
                    if (! this@VirtualWatchmanSet.isDestroyed) dismissDialog()
                    showDialogError("Something went wrong, Please try again")
                }

                override fun onResponse(call: Call<VirtualWatchmanActivationModel>, response: Response<VirtualWatchmanActivationModel>) {
                    if (! this@VirtualWatchmanSet.isDestroyed) dismissDialog()
                    if (response.isSuccessful && response.body() != null && response.body()?.error?.code == 0) {
                        val virtualWatchmanActivationModel: VirtualWatchmanActivationModel? = response.body()
                        Log.d("iyiliy", "" + virtualWatchmanActivationModel?.response?.parking?.parkingID)

                        var expireDateTime : String = convertDateTimeFormate(virtualWatchmanActivationModel?.response?.parking?.parkingTimeExpiry.toString())
                        var serverDateTime : String = convertDateTimeFormate(virtualWatchmanActivationModel?.response?.parking?.parkingTime.toString())

                        val intent = Intent(this@VirtualWatchmanSet, VirtualWatchmanActivation::class.java)
                        intent.putExtra("terminalId", body.terminalID)
                        intent.putExtra("radiusMeterOnlyFirstTime", radiusMeter)
                        intent.putExtra("cookie", mPrefRepository.cookie)
                        intent.putExtra("ParkingTimeExpiry", expireDateTime)
                        intent.putExtra("serverDateTime", serverDateTime)
                        intent.putExtra("selectedVehicleLatitude", selectedVehicleLatitude)
                        intent.putExtra("selectedVehicleLongitude", selectedVehicleLongitude)
                        startActivity(intent)
                        finish()
                    }
                }

            })

        }
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


