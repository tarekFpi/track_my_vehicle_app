package com.singularity.trackmyvehicle.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaurav.gesto.OnGestureListener
import com.google.gson.Gson
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import com.whiteelephant.monthpicker.MonthPickerDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_expense_list.view.*
import kotlinx.android.synthetic.main.fragment_monthly_report.*
import kotlinx.android.synthetic.main.item_monthly_distance_table.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import javax.inject.Inject

class MonthlyReportFragment : Fragment() {

    private var mMonthlyReportLiveData: MutableLiveData<Resource<List<DistanceReport>>>? = null

    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel

    private var mSelectedDateTime: DateTime? = null

    var temporaryVehicleData : List<DistanceReport> = ArrayList()
    var updatedVehicleData : ArrayList<DistanceReport> = ArrayList()

    var vehicleKmList : ArrayList<Float> = ArrayList()

    private fun showChart() {
        Log.d("iuuyhj","entered 1")
        rvEngineOfTimeLine.visibility = if (mAdapter.itemCount == 0) View.GONE else View.VISIBLE
        Log.d("iuuyhj", "${mAdapter.itemCount}")
        txtEmptyText.visibility = if (mAdapter.itemCount != 0) View.GONE else View.VISIBLE
        progressBar.visibility = View.GONE

    }

    private fun hideChart() {
        rvEngineOfTimeLine.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        txtEmptyText.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_monthly_report, container, false)
    }

    private val mAdapter = MonthlyDistanceAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgClose.setOnClickListener { this.activity?.finish() }

        hideChart()
        mSelectedDateTime = DateTime.now()

        updateDateLabels()

        updateMonthlyReport()

        showChart()

        rvEngineOfTimeLine.layoutManager = LinearLayoutManager(requireContext())

        rvEngineOfTimeLine.adapter = mAdapter

        containerMonthAndYear.setOnClickListener {
            val builder = MonthPickerDialog.Builder(requireContext(),
                    MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                        mSelectedDateTime = DateTime.now().withMonthOfYear(selectedMonth + 1)
                        .withYear(selectedYear)
                        updateDateLabels()
                        updateMonthlyReport()
                    },
                    mSelectedDateTime?.year ?: DateTime.now().year,
                    (mSelectedDateTime?.monthOfYear ?: DateTime.now().monthOfYear) - 1)
            builder.setTitle("Select Month")
            builder.setMinYear(DateTime.now().minusMonths(3).year - 14)
            builder.setMaxYear(DateTime.now().year + 14)
            builder.build().show()
        }

        swipeRefreshLayout.setOnRefreshListener {
            updateMonthlyReport()
        }

        txtCurrentBstId.setOnTouchListener(object : OnGestureListener(requireContext()) {
            override fun onSwipeBottom() {
                mVehicleViewModel.selectNextVehicle()

            }

            override fun onSwipeTop() {
                mVehicleViewModel.selectPreviousVehicle()
            }
        })

        updateVehicleList()

        searchByBstidVrn()
    }

    private fun searchByBstidVrn() {
        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(searchItem : CharSequence?, start: Int, before: Int, count: Int) {
                updateVehicleList(searchItem.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun updateDateLabels() {
        txtMonth.text = mSelectedDateTime?.toString("MMM")
        txtYear.text = mSelectedDateTime?.toString("yyyy")
    }

    companion object {
        fun newInstance(): MonthlyReportFragment {
            val fragment = MonthlyReportFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {

    }

    private val mObserver = Observer<Resource<List<DistanceReport>>> {
        val data = it.data ?: listOf()

        temporaryVehicleData = data

        /*mAdapter.setItems(data.sortedBy { it1 -> it1.bstId }?.toMutableList())
        if (data.isEmpty() && it.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
        }*/
        updateVehicleList(etSearch.text.toString())
        if (temporaryVehicleData.isEmpty() && it.status == Status.LOADING) {
            hideChart()
        } else {
            showChart()
        }

        swipeRefreshLayout.isRefreshing = it.status == Status.LOADING

        if (it.status == Status.LOADING && data.isEmpty()) {
            mAdapter.countPosition=0
            Toasty.warning(requireContext(), "Loading information").show()
            return@Observer
        }
        if (it.status == Status.SUCCESS && data.isNullOrEmpty()) {
            Toasty.warning(requireContext(), "Vehicle data not available").show()
        } else if (it.status == Status.SUCCESS) {
            Toasty.success(requireContext(), "Information loaded").show()

            mAdapter.countPosition=0
            return@Observer
        }
        if (it.status == Status.ERROR) {
            Toasty.error(requireContext(), "Engine report loading failed").show()
        }

    }

    private fun updateVehicleList(query : String = "") {

        updatedVehicleData.clear()

        vehicleKmList.clear()


        temporaryVehicleData?.forEach { vehicle ->

             if(vehicle.TerminalAssignmentIsSuspended.toString().equals("0")){

                 vehicleKmList.add(vehicle.km.toFloat())

                 if ((vehicle.vrn ?: "").toLowerCase()
                         ?.contains(query.toLowerCase()) ||
                     vehicle.bstId.toLowerCase().contains(
                         query.toLowerCase()
                     )) {


                     updatedVehicleData.add(vehicle)
                 }

                 mAdapter.countPosition=0
             }



        }



        var totalDistance = vehicleKmList.sum()

        textView_totalDistance_value.text = String.format("%.2f km", totalDistance)

        mAdapter.setItems(updatedVehicleData.sortedBy { it1 ->

            it1.bstId }?.toMutableList())
    }

    private fun updateMonthlyReport() {

        mMonthlyReportLiveData?.removeObserver(mObserver)
        mMonthlyReportLiveData = mReportsViewModel.fetchMonthlyReport(
                mSelectedDateTime ?: DateTime.now())
        mMonthlyReportLiveData?.observe(this, mObserver)

    }

}

class MonthlyDistanceAdapter : RecyclerView.Adapter<MonthlyDistanceAdapter.TimeLineViewHolder>() {

    private var mData = mutableListOf<DistanceReport>()

    var  countPosition:Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_monthly_distance_table, parent, false)
        return TimeLineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {


        holder.bind(mData[position], position)

        if(position%2 == 0){
            holder.itemView.setBackgroundColor(Color.parseColor("#F2F2F2"))

        }else{
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }

    fun setItems(data: MutableList<DistanceReport>) {
        mData = data
        notifyDataSetChanged()
    }

    override fun getItemCount() = mData.size //if (mData.size == 0) 0 else (mData.size + 1)

    inner class TimeLineViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        //val txtStart = containerView.txtStart
        //val txtTo = containerView.txtTo
        val txtDistance = containerView.txtDistance
        val txtBstId = containerView.txtBstId
        val txtSl = containerView.textView_sl
        val txtVrn = containerView.textView_vrn



        fun bind(model: DistanceReport, position : Int) {


            if(model.TerminalAssignmentIsSuspended.toString().equals("0")){

                countPosition++

                txtBstId.text = model.bstId.trim()
                txtDistance.text = String.format("%.2f km", model.km?.toFloat() ?: 0f).trim()
                txtSl.text = "${countPosition}"
                txtVrn.text = model.vrn.trim()
                val parse = DateTime.parse(model.date)

                Log.d("lkhnl","${Gson().toJson(model)}")
            }else{


                txtBstId.visibility=View.GONE
                txtDistance.visibility=View.GONE
                txtSl.visibility=View.GONE
                txtVrn.visibility=View.GONE
            }


          //  Log.d("lkhnl","${Gson().toJson(model)}")

            //txtStart.text = parse.withDayOfMonth(1).toString("dd-MMM")
            //txtTo.text = parse.withDayOfMonth(parse.dayOfMonth().maximumValue).toString("dd-MMM")

        }

        fun bindHeader() {
            txtBstId.text = ""
            txtDistance.text = ""
            //txtStart.text = "From"
            //txtTo.text = "To"
            txtSl.text = ""
            txtVrn.text = ""
        }
    }

}