package com.singularity.trackmyvehicle.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.R.id.*
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.VehicleCurrentStatusModel
import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.OnItemClickListener
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.view.adapter.VehicleCurrentStatusAdapter
import com.singularity.trackmyvehicle.view.adapter.VehicleListAdapter
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendExpiredDialog
import com.singularity.trackmyvehicle.view.dialog.SuspendedMaintenanceDialog
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_analytics.*
import kotlinx.android.synthetic.main.fragment_bottom_nav.*
import kotlinx.android.synthetic.main.fragment_bottom_nav.recyclerView_vehicleStatus
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_home_content.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject


class BottomNavFragment constructor(): BottomSheetDialogFragment(), OnItemClickCallback<Terminal>, TextWatcher {



    private val job = SupervisorJob()
    private  val scope = CoroutineScope(Dispatchers.IO + job)

    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mAnalytics: AnalyticsViewModel

    @Inject
    lateinit var mAdapter: VehicleListAdapter

    private var mData: List<Terminal>? = ArrayList()
    private var mTravelData: HashMap<Int, Float?> = hashMapOf()

    var VEHICLE_TYPE = ALL_VEHICLES_TYPE

    var SORT_TYPE = 0

    var mMapDrawer: MapDrawer? = null

    val suspendExpiredDialog = SuspendExpiredDialog()

    val suspendMaintenanceDialog = SuspendedMaintenanceDialog()

    private lateinit var appPreference : AppPreference

    lateinit var vehicleCurrentStatusAdapter: VehicleCurrentStatusAdapter

    private var vehicles = ArrayList<Terminal>()
    private var movingVehicles = ArrayList<Terminal>()
    private var offlineVehicles = ArrayList<Terminal>()
    private var idleVehicles = ArrayList<Terminal>()
    private var engineOffVehicles = ArrayList<Terminal>()
    private var suspendedVehicles = ArrayList<Terminal>()

    private var vehicleListTemporary: ArrayList<Terminal> = ArrayList()

    var vehicleCurrentStatusList: ArrayList<VehicleCurrentStatusModel> = ArrayList()

    var data = mutableListOf<Terminal>()

    @Inject
    lateinit var executors: AppExecutors

    private val mVehicleObserver = Observer<List<Terminal>> { data ->
        if (data == null) {
            return@Observer
        }

        mData = data


        if (mData?.size == 0) {
            return@Observer
        } else {
            getCurrentVehicleStatus(mData as MutableList<Terminal>)
        }

        updateVehicleList(query = mEtSearch?.text.toString())
    }

    private val mVehicleTodayTravelDistanceObserver =
        Observer<List<TerminalAggregatedData>> { data ->
            val output = hashMapOf<Int, Float?>()
            data?.forEach {
                it.terminalID.toIntOrNull()?.let { id ->
                    output[id] = it.terminalDataMinutelyDistanceMeter?.toFloatOrNull()
                }
            }
            mTravelData = output
            updateVehicleList()
        }

    var suspendView: RelativeLayout? = null

    var blurView: ImageView? = null


    var vehicleModeCard: CardView? = null
    var mapDetailsCard: MaterialCardView? = null
    var refreshCard: MaterialCardView? = null
    var currentLocationCard: MaterialCardView? = null


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        VehicleTrackApplication.appComponent?.inject(this)
        super.onCreate(savedInstanceState)
        mVehicleViewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(VehiclesViewModel::class.java)

        mVehicleViewModel.fetch(true)
    }

    private var mEtSearch: EditText? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = inflater.inflate(R.layout.fragment_bottom_nav, container, false)

        val mainContent = contentView.findViewById<View>(R.id.viewMainContent)
        val vehicles: RecyclerView = contentView.findViewById(R.id.listVehicle)


        vehicles.adapter = mAdapter
        vehicles.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        vehicles.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        mAdapter.callback = this

       mVehicleViewModel.getVehicles().observe(this, mVehicleObserver)

        mVehicleViewModel.getTodaysTravelledDistance()
            .observe(this, mVehicleTodayTravelDistanceObserver)


        mEtSearch = contentView.findViewById(R.id.etSearch)
        mEtSearch?.addTextChangedListener(this)

        vehicleModeCard = requireActivity().findViewById<CardView>(R.id.vehicleModeCard)
        mapDetailsCard = requireActivity().findViewById<MaterialCardView>(R.id.mapDetailsCard)
        refreshCard = requireActivity().findViewById<MaterialCardView>(R.id.refreshCard)
        currentLocationCard =requireActivity().findViewById<MaterialCardView>(R.id.currentLocationCard)

        return contentView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       updateVehicleList()
        view.invalidate()

        vehicleCurrentStatusAdapter =
            VehicleCurrentStatusAdapter(view.context, onMenuClickListener, true)
        vehicleCurrentStatusAdapter.updateVehicleList(vehicleCurrentStatusList)

        sortSelect()

        updateView()

    }


    private fun sortSelect() {

        texView_speed.setOnClickListener {

            progressBarId.visibility = View.VISIBLE

            SORT_TYPE = 1

            updateView()


        }

        textView_default.setOnClickListener {

          progressBarId.visibility = View.VISIBLE

            SORT_TYPE = 0

            updateView()

        }
    }

    private fun updateView() {

            textView_default.background =
                resources.getDrawable(if (SORT_TYPE == 0) R.drawable.bg_rectangle_fill_color_primary else R.drawable.bg_rectangle_border_color_primary)
            textView_default.setTextColor(
                if (SORT_TYPE == 0) resources.getColor(R.color.analyticsButtonSpecificColor) else resources.getColor(
                    R.color.black
                )
            )
            texView_speed.background =
                resources.getDrawable(if (SORT_TYPE == 1) R.drawable.bg_rectangle_fill_color_primary else R.drawable.bg_rectangle_border_color_primary)
            texView_speed.setTextColor(
                if (SORT_TYPE == 1) resources.getColor(R.color.analyticsButtonSpecificColor) else resources.getColor(
                    R.color.black
                )
            )

      /// if(scope.isActive) scope.cancel()
     scope.launch {

         if (SORT_TYPE == 0)
         {
             sortedByBstid(data)
             progressBarId.visibility = View.GONE

         }else{

             sortedBySpeed(data)
             progressBarId.visibility = View.GONE
         }
        }

 //  sortedBySpeed(data)
    }

    private fun getCurrentVehicleStatus(data: MutableList<Terminal>) {
        suspendedVehicles.clear()
        offlineVehicles.clear()
        idleVehicles.clear()
        movingVehicles.clear()
        engineOffVehicles.clear()
        vehicles.clear()

        for (item in data) {
            val lastUpdatedDate = item.terminalDataTimeLast?.toString("yyyy-MM-dd HH:mm:ss")

            var updatedDate = try {
                DateTime.parse(lastUpdatedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
            } catch (ex: Exception) {
                null
            }

            if (!item.isSuspended()) {
                if (updatedDate?.isBefore(DateTime.now().minusDays(1)) == true) {
                    offlineVehicles.add(item)

                } else if (item.terminalDataIsAccOnLast == "1" && item.terminalDataVelocityLast?.toDouble() ?: 0.0 == 0.0) {
                    idleVehicles.add(item)

                } else if (item.terminalDataIsAccOnLast == "1") {
                    movingVehicles.add(item)

                } else {
                    engineOffVehicles.add(item)
                }
            } else {
                suspendedVehicles.add(item)
            }

            if (item.terminalDataLatitudeLast?.isNotEmpty() == true && item.terminalDataLongitudeLast?.isNotEmpty() == true) {
                vehicles.add(item)
            }
        }

        assignVehicleList()

        vehicleCurrentStatusList.clear()

        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                ALL_VEHICLES_TYPE,
                vehicles.size,
                "All Vehicles"
            )
        )
        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                MOVING_VEHICLES_TYPE,
                movingVehicles.size,
                "Moving"
            )
        )
        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                IDLE_VEHICLES_TYPE,
                idleVehicles.size,
                "Idle"
            )
        )
        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                ENGINE_OFF_VEHICLES_TYPE,
                engineOffVehicles.size,
                "Engine Off"
            )
        )
        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                OFFLINE_VEHICLES_TYPE,
                offlineVehicles.size,
                "Offline"
            )
        )
        vehicleCurrentStatusList.add(
            VehicleCurrentStatusModel(
                SUSPENDED_VEHICLES_TYPE,
                suspendedVehicles.size,
                "Suspended"
            )
        )

        recyclerView_vehicleStatus.isNestedScrollingEnabled = false
        recyclerView_vehicleStatus.setHasFixedSize(true)
        recyclerView_vehicleStatus.adapter = vehicleCurrentStatusAdapter
        //vehicleCurrentStatusAdapter?.notifyDataSetChanged()
    }

    private val onMenuClickListener = object : OnItemClickListener {
        override fun onItemClickListener(view: View, position: Int) {

         val item = vehicleCurrentStatusList?.get(position)

           onItemClickedFunction(view.context, item)
        }
    }

    fun onItemClickedFunction(context: Context, item: VehicleCurrentStatusModel) {
        VEHICLE_TYPE = item.id ?: 0
        assignVehicleList()

        updateVehicleList()

    }

    private fun assignVehicleList() {
        vehicleCurrentStatusAdapter.setSelectedView(VEHICLE_TYPE)

        if (VEHICLE_TYPE == ALL_VEHICLES_TYPE) {
            vehicleListTemporary = vehicles

        } else if (VEHICLE_TYPE == MOVING_VEHICLES_TYPE) {
            vehicleListTemporary = movingVehicles

        } else if (VEHICLE_TYPE == IDLE_VEHICLES_TYPE) {
            vehicleListTemporary = idleVehicles

        } else if (VEHICLE_TYPE == ENGINE_OFF_VEHICLES_TYPE) {
            vehicleListTemporary = engineOffVehicles

        } else if (VEHICLE_TYPE == OFFLINE_VEHICLES_TYPE) {
            vehicleListTemporary = offlineVehicles

        } else if (VEHICLE_TYPE == SUSPENDED_VEHICLES_TYPE) {
            vehicleListTemporary = suspendedVehicles

        }

    }

    private fun updateVehicleList(query: String = "") {

        data.clear()

        vehicleListTemporary?.forEach { vehicle ->
            vehicle.travelled = mTravelData[vehicle.terminalID]
            if ((vehicle.carrierRegistrationNumber ?: "").toLowerCase()
                    ?.contains(query.toLowerCase()) ||
                vehicle.bstId.toLowerCase().contains(
                    query.toLowerCase()
                )
            ) {
                data.add(vehicle)
            }
        }

         if (data.isEmpty()) {

             txtEmpty.visibility =  View.VISIBLE
            progressBarId.visibility = View.VISIBLE

          } else {

             txtEmpty.visibility =  View.GONE
             progressBarId.visibility =  View.GONE
         }


        if (SORT_TYPE == 0) {
          sortedByBstid(data)

        } else {
            sortedBySpeed(data)

        }

    }

    private fun  sortedBySpeed(data: MutableList<Terminal>) {


     mAdapter.setData(data.sortedByDescending { it.terminalDataVelocityLast })


      Log.d("speedError:","speed:${data.sortedByDescending { it.terminalDataVelocityLast }}")

    }

    private fun sortedByBstid(data: MutableList<Terminal>) {

      mAdapter.setData(data.sortedBy { it.bstid })

    }

    val suspendDialog = SuspendDialog()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(model: Terminal) {

     appPreference = AppPreferenceImpl(requireContext())

        if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.robi.robitrackervts")){


            if(model?.terminalAssignmentIsSuspended == "1"){

                var TerminalTimeLast = model?.terminalDataTimeLast.toString()

                if(TerminalTimeLast.equals("")  || TerminalTimeLast.equals("null")){

                    suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                   //  Toast.makeText(requireContext(), "NEED MAINTENANCE date null ", Toast.LENGTH_SHORT).show()

                }else {


                    var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                    var currentDate = Calendar.getInstance()
                    var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    var datetime = Currnetdateformat.format(currentDate.time)


                    var currentDateTime: Date = Currnetdateformat.parse(datetime)

                    var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                    var hasUpdateIn24Hour: Long?   = difference / (60 * 60 * 1000)

                    if(hasUpdateIn24Hour!! <=24){

                        if(!suspendExpiredDialog.shown){

                            suspendExpiredDialog.show(requireFragmentManager(), "SuspendExpire")
                        //    Toast.makeText(requireContext(), "SuspendMaintenance: ${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()
                        }
                    }else{

                        // Toast.makeText(requireContext(), "SuspendExpire date 24 hours not data :${hasUpdateIn24Hour}", Toast.LENGTH_SHORT).show()

                        suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                    }
                }

            }else{

                updateVehicle(model)
                if (this@BottomNavFragment.isVisible) {
                    this.dismiss()
                }

            }

        }else if(BuildConfig.APPLICATION_ID.equals("com.singularitybd.ral.trackmyvehicle")) {

            if(model?.terminalAssignmentIsSuspended == "1"){


                var TerminalTimeLast = model?.terminalDataTimeLast.toString()

                if(TerminalTimeLast.equals("")  || TerminalTimeLast.equals("null")){

                    suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                 //   Toast.makeText(requireContext(), "SuspendMaintenance: null date", Toast.LENGTH_SHORT).show()

                }else{

                    var TerminalDataFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    var TerminalListDate: Date = TerminalDataFormat.parse(TerminalTimeLast)

                    var currentDate = Calendar.getInstance()
                    var Currnetdateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    var datetime = Currnetdateformat.format(currentDate.time)


                    var currentDateTime: Date = Currnetdateformat.parse(datetime)

                    var difference: Long = currentDateTime.getTime()- TerminalListDate.getTime()

                    var hasUpdateIn24Hour: Long = difference / (60 * 60 * 1000)

                    if(hasUpdateIn24Hour <= 24){

                        if(!suspendExpiredDialog.shown){

                            suspendExpiredDialog.show(requireFragmentManager(), "SuspendExpire")
                        }
                    }else{

                        suspendMaintenanceDialog.show(requireFragmentManager(), "SuspendMaintenance")

                    }
                }

            }else{
                updateVehicle(model)
                if (this@BottomNavFragment.isVisible) {
                    this.dismiss()
                }
            }

        }else{

            //================ tmv apps Suspended Condition================//

            if ((model.terminalAssignmentIsSuspended == "1")) {
                if (!suspendDialog.shown) {

                    appPreference.SetBstId(AppPreference.bstId,model?.bstId.toString())

                    suspendDialog.show(requireFragmentManager(), "SUSPEND_DIALOG")

                }
            } else {


                updateVehicle(model)
                if (this@BottomNavFragment.isVisible) {
                    this.dismiss()
                }
            }
        }

    }

    fun updateVehicle(model: Terminal) {

        suspendView = requireActivity().findViewById<RelativeLayout>(R.id.suspendView)
        blurView = requireActivity().findViewById<ImageView>(R.id.blur_view)

        if((model.terminalAssignmentIsSuspended == "0")){
            suspendView?.visibility = View.GONE
            blurView?.visibility = View.GONE

            if (this@BottomNavFragment.isVisible) {
                this.dismiss()
            }

            mAnalytics.vehicleSelected()
            mVehicleViewModel.changeCurrentVehicle(model.bstid, model.vrn, model.bid)


            vehicleModeCard?.visibility = View.VISIBLE
            mapDetailsCard?.visibility = View.VISIBLE
            refreshCard?.visibility =View.VISIBLE
            currentLocationCard?.visibility = View.VISIBLE
        }

    }


    companion object {
        fun newInstance(): BottomNavFragment {
            val fragment = BottomNavFragment()
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {
            clearFindViewByIdCache()

        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            ex.printStackTrace()
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        updateVehicleList(p0.toString())
    }


    override fun onResume() {
        super.onResume()
        mAnalytics.vehicleChangeScreenViewed()
        updateVehicleList("")
    }
}