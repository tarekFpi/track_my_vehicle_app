package com.singularity.trackmyvehicle.view.fragment

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.implementation.v3.NotificationResponseWrapper
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.utils.disableFCM
import com.singularity.trackmyvehicle.utils.enableFCM
import com.singularity.trackmyvehicle.view.adapter.NotificationListAdapter
import com.singularity.trackmyvehicle.view.customview.EndlessRecyclerOnScrollListener
import com.singularity.trackmyvehicle.view.customview.behaviour.BottomSheetBehaviorGoogleMapsLike
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.NotificationViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.alert_dialog_notification_turned_on_off.*
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.fragment_notification_list.*
import kotlinx.android.synthetic.main.fragment_notification_list.emptyView
import kotlinx.android.synthetic.main.fragment_notification_list.etSearch
import kotlinx.android.synthetic.main.fragment_notification_list.rvNotificationList
import kotlinx.android.synthetic.main.fragment_notification_list.srlNotificationList
import kotlinx.android.synthetic.main.layout_home_content.*
import kotlinx.android.synthetic.main.layout_notification_filter.*
import javax.inject.Inject


class NotificationFragment : Fragment(), OnItemClickCallback<Notification> {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var mFCMRepository: FCMRepository
    @Inject
    lateinit var mPrefRepository: PrefRepository

    lateinit var mNotificationViewModel: NotificationViewModel

    private var mAdapter: NotificationListAdapter = NotificationListAdapter(this)

    private var mData: MutableList<Notification> = mutableListOf()
    private var allNotificationData: MutableList<Notification> = mutableListOf()

    private var lastCount = 0

    var checkAllNotification = false
    var checkEngineStartNotification = false
    var checkEngineStopNotification = false
    var checkPanicOnNotification = false
    var checkOverSpeedNotification = false
    var checkParkingExitNotification = false
    var checkPowerDownNotification = false
    var checkVirtualFenceNotification = false

    var isBottomSheetNotificationShow = false

    var isNotificationDisable = true

    private lateinit var appPreference : AppPreference

    private var mNotificationListLiveData: LiveData<Resource<NotificationResponseWrapper>>? = null
    private var mNotificationListLiveDataObserver: Observer<Resource<NotificationResponseWrapper>> = Observer { data ->
        srlNotificationList.isRefreshing = data.status == Status.LOADING
        data?.let {
            data.data?.let { it1 ->
                lastCount = it1.count ?: 0
                if (it1.firstPage) {
                    mData = mutableListOf()
                }
                val map = it1.data?.mapNotNull { m -> m.id?.toString() }
                val ids = map?.joinToString(",")
                ids?.let { _ids ->
                    mNotificationViewModel.markMessagesAsRead(_ids)
                }

                mData.addAll(it1.data ?: listOf())
                mData = mData.distinctBy { m -> m.id }.sortedByDescending { m -> m.time }
                        .toMutableList()
                mAdapter.setItems(mData)

                getFilteredNotifications(it1.data ?: listOf())

            }
            when (it.status) {
                Status.SUCCESS -> {
                }
                Status.ERROR -> {
                    Toasty.error(requireContext(), data.message ?: "Something went wrong").show()
                }
                Status.LOADING -> {
                    Toasty.warning(requireContext(), "Loading").show()
                }
            }
        }

        updateEmptyView(data?.data?.data?.size ?: 0)

    }

    private fun updateEmptyView(itemCount: Int) {
        if (itemCount == 0) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        initiateViewModel()
    }

    private fun initiateViewModel() {
        mNotificationViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(NotificationViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNotificationListRecyclerView()
        fetchNotifications()

        srlNotificationList.setOnRefreshListener {
            fetchNotifications(query = etSearch.text.toString())
            srlNotificationList.isRefreshing = false
        }

        etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            fetchNotifications(query = etSearch.text.toString())
            return@setOnEditorActionListener true
        }

        appPreference = AppPreferenceImpl(requireContext())

        setBottomSheetNotificationFilter()

        filterNotificationCheck()

        setNotificationAlertDialog()
    }

    private fun setNotificationAlertDialog() {

        var textNotificationTurnedOnTitle = resources.getString(R.string.notifications_turned_on_title)
        var textNotificationTurnedOffTitle = resources.getString(R.string.notifications_turned_off_title)

        var textNotificationTurnedOnSubTitle = resources.getString(R.string.text_notification_turned_on)
        var textNotificationTurnedOffSubTitle = resources.getString(R.string.text_notification_turned_off)

        var imageNotificationTurnedOn = resources.getDrawable(R.drawable.ic_notification_turned_on)
        var imageNotificationTurnedOff = resources.getDrawable(R.drawable.ic_notification_turned_off)

        if(appPreference.getBoolean(AppPreference.isNotificationEnable) == false){
            imageView_notification_onOff.setImageDrawable(resources.getDrawable(R.drawable.ic_notification_disable))
            isNotificationDisable = false

        }else{
            imageView_notification_onOff.setImageDrawable(resources.getDrawable(R.drawable.ic_notification_enable))
            isNotificationDisable = true
        }

        imageView_notification_onOff.setOnClickListener {
            if(appPreference.getBoolean(AppPreference.isNotificationEnable) == false){
                showNotificationTurnedOnOffDialog(textNotificationTurnedOnSubTitle, imageNotificationTurnedOn, textNotificationTurnedOnTitle)
                isNotificationDisable = false

            }else{
                showNotificationTurnedOnOffDialog(textNotificationTurnedOffSubTitle, imageNotificationTurnedOff, textNotificationTurnedOffTitle)
                isNotificationDisable = true
            }


        }
    }

    private fun showNotificationTurnedOnOffDialog(textNotificationTurnedOnSubTitle: String, imageNotificationTurnedOn: Drawable, textNotificationTurnedOnTitle: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.alert_dialog_notification_turned_on_off)

        val textViewNotificationTitle = dialog.findViewById(R.id.textView_notification_title) as TextView
        val textViewNotificationSubTitle = dialog.findViewById(R.id.textView_notification_subTitle) as TextView
        val imageViewNotification = dialog.findViewById(R.id.imageView_notification) as ImageView

        textViewNotificationTitle.text = textNotificationTurnedOnTitle
        textViewNotificationSubTitle.text = textNotificationTurnedOnSubTitle
        imageViewNotification.setImageDrawable(imageNotificationTurnedOn)

        val buttonContinue = dialog.findViewById(R.id.textView_continue) as TextView

        buttonContinue.setOnClickListener {
            if(isNotificationDisable){
                isNotificationDisable = false
                imageView_notification_onOff.setImageDrawable(resources.getDrawable(R.drawable.ic_notification_disable))

                appPreference.setBoolean(AppPreference.isNotificationEnable, false)

                disableFCM()

            }else{
                isNotificationDisable = true
                imageView_notification_onOff.setImageDrawable(resources.getDrawable(R.drawable.ic_notification_enable))

                appPreference.setBoolean(AppPreference.isNotificationEnable, true)

                enableFCM(mFCMRepository, appPreference, mPrefRepository)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setBottomSheetNotificationFilter() {
        val behavior = BottomSheetBehaviorGoogleMapsLike.from<View>(bottom_sheet_notification_filter)
        behavior.addBottomSheetCallback(object :
                BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED    -> Log.d("bottomsheet-",
                            "STATE_COLLAPSED")
                    BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING     -> Log.d("bottomsheet-",
                            "STATE_DRAGGING")
                    BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED     -> {
                        Log.d("bottomsheet-", "STATE_EXPANDED")
                    }
                    BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT -> Log.d("bottomsheet-",
                            "STATE_ANCHOR_POINT")
                    BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN       -> Log.d("bottomsheet-",
                            "STATE_HIDDEN")
                    else                                                 -> Log.d("bottomsheet-", "STATE_SETTLING")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


        imageView_filter_button.setOnClickListener {

            if(isBottomSheetNotificationShow){
                bottom_sheet_notification_filter.visibility = View.GONE
                isBottomSheetNotificationShow = false

            }else{
                bottom_sheet_notification_filter.visibility = View.VISIBLE
                isBottomSheetNotificationShow = true
            }

        }
    }

    private fun getFilteredNotifications(list: List<Notification>) {

        textView_select.setOnClickListener {

            allNotificationData.addAll(list)

            mData.clear()

            for(item in allNotificationData){

                var filterNotification : Array<String>? = item.subject?.split("|")?.toTypedArray()

                if(checkEngineStartNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "engine start") {

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkEngineStopNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "engine stop"){

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkPanicOnNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "panic on"){

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkOverSpeedNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "overspeed"){

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkPowerDownNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "powerdown"){

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkVirtualFenceNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "virtual fence"){

                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkParkingExitNotification == true){

                    if(filterNotification?.get(0)?.trim()?.toLowerCase() == "parking exit"){


                        mData.addAll(listOf(item))
                        mAdapter.setItems(mData)
                        mAdapter.notifyDataSetChanged()

                    }

                }

                if(checkAllNotification == true){

                    mData.addAll(listOf(item))
                    mAdapter.setItems(mData)
                    mAdapter.notifyDataSetChanged()

                }

                if(checkEngineStartNotification == false && checkEngineStopNotification == false && checkPanicOnNotification == false && checkOverSpeedNotification == false && checkPowerDownNotification == false && checkVirtualFenceNotification == false && checkParkingExitNotification == false){
                    mData.addAll(listOf(item))
                    mAdapter.setItems(mData)
                    mAdapter.notifyDataSetChanged()
                }

                updateEmptyView(mData.size)
                mAdapter.notifyDataSetChanged()

            }

            bottom_sheet_notification_filter.visibility = View.GONE
            isBottomSheetNotificationShow = false
        }

    }

    private fun filterNotificationCheck() {

        radioButton_selectAllNotification.setOnClickListener {
            if (checkAllNotification) {
                checkAllNotification = false
                radioButton_selectAllNotification.isChecked = false
                radioButton_engineOn.isChecked = false
                radioButton_engineOff.isChecked = false
                radioButton_panic.isChecked = false
                radioButton_overSpeed.isChecked = false
                radioButton_Offline.isChecked = false
                radioButton_disconnect.isChecked = false
                radioButton_virtualFence.isChecked = false


                 checkEngineStartNotification = false
                 checkEngineStopNotification = false
                 checkPanicOnNotification = false
                 checkOverSpeedNotification = false
                 checkParkingExitNotification = false
                 checkPowerDownNotification = false
                 checkVirtualFenceNotification = false

                textView_engineStart.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_engineStop.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_panicOn.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_overSpeed.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_powerDown.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_virtualFence.setTextColor(resources.getColor(R.color.LabelTextAshColor))
                textView_parking_exit.setTextColor(resources.getColor(R.color.LabelTextAshColor))


            } else {
                checkAllNotification = true
                radioButton_selectAllNotification.isChecked = true
                radioButton_engineOn.isChecked = true
                radioButton_engineOff.isChecked = true
                radioButton_panic.isChecked = true
                radioButton_overSpeed.isChecked = true
                radioButton_Offline.isChecked = true
                radioButton_disconnect.isChecked = true
                radioButton_virtualFence.isChecked = true

                checkEngineStartNotification = true
                checkEngineStopNotification = true
                checkPanicOnNotification = true
                checkOverSpeedNotification = true
                checkParkingExitNotification = true
                checkPowerDownNotification = true
                checkVirtualFenceNotification = true

                textView_engineStart.setTextColor(resources.getColor(R.color.black))
                textView_engineStop.setTextColor(resources.getColor(R.color.black))
                textView_panicOn.setTextColor(resources.getColor(R.color.black))
                textView_overSpeed.setTextColor(resources.getColor(R.color.black))
                textView_powerDown.setTextColor(resources.getColor(R.color.black))
                textView_virtualFence.setTextColor(resources.getColor(R.color.black))
                textView_parking_exit.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_engineOn.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkEngineStartNotification){
                checkEngineStartNotification = false
                radioButton_engineOn.isChecked = false

                textView_engineStart.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkEngineStartNotification = true
                radioButton_engineOn.isChecked = true

                textView_engineStart.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_engineOff.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkEngineStopNotification){
                checkEngineStopNotification = false
                radioButton_engineOff.isChecked = false

                textView_engineStop.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkEngineStopNotification = true
                radioButton_engineOff.isChecked = true

                textView_engineStop.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_panic.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkPanicOnNotification){
                checkPanicOnNotification = false
                radioButton_panic.isChecked = false

                textView_panicOn.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkPanicOnNotification = true
                radioButton_panic.isChecked = true

                textView_panicOn.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_overSpeed.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkOverSpeedNotification){
                checkOverSpeedNotification = false
                radioButton_overSpeed.isChecked = false

                textView_overSpeed.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkOverSpeedNotification = true
                radioButton_overSpeed.isChecked = true

                textView_overSpeed.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_Offline.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkParkingExitNotification){
                checkParkingExitNotification = false
                radioButton_Offline.isChecked = false

                textView_parking_exit.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkParkingExitNotification = true
                radioButton_Offline.isChecked = true

                textView_parking_exit.setTextColor(resources.getColor(R.color.black))

            }
        }

        radioButton_disconnect.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkPowerDownNotification){
                checkPowerDownNotification = false
                radioButton_disconnect.isChecked = false

                textView_powerDown.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkPowerDownNotification = true
                radioButton_disconnect.isChecked = true

                textView_powerDown.setTextColor(resources.getColor(R.color.black))

                if (checkAllNotification == true) {
                    checkAllNotification = false
                    radioButton_selectAllNotification.isChecked = false
                    radioButton_engineOn.isChecked = false
                    radioButton_engineOff.isChecked = false
                    radioButton_panic.isChecked = false
                    radioButton_overSpeed.isChecked = false
                    radioButton_Offline.isChecked = false
                    radioButton_virtualFence.isChecked = false

                }

            }
        }

        radioButton_virtualFence.setOnClickListener {
            radioButton_selectAllNotification.isChecked = false
            checkAllNotification = false

            if(checkVirtualFenceNotification){
                checkVirtualFenceNotification = false
                radioButton_virtualFence.isChecked = false

                textView_virtualFence.setTextColor(resources.getColor(R.color.LabelTextAshColor))

            }else{
                checkVirtualFenceNotification = true
                radioButton_virtualFence.isChecked = true

                textView_virtualFence.setTextColor(resources.getColor(R.color.black))

                if (checkAllNotification == true) {
                    checkAllNotification = false
                    radioButton_selectAllNotification.isChecked = false
                    radioButton_engineOn.isChecked = false
                    radioButton_engineOff.isChecked = false
                    radioButton_panic.isChecked = false
                    radioButton_overSpeed.isChecked = false
                    radioButton_Offline.isChecked = false
                    radioButton_disconnect.isChecked = false

                }

            }

        }

    }

    private fun fetchNotifications(offset: Int = 1, query: String = "") {
        if (offset == 1) {
            mData = mutableListOf()
            lastCount = 0
            mAdapter.setItems(mData)
        }
        updateEmptyView(mData.size)
        mNotificationListLiveData?.removeObserver(mNotificationListLiveDataObserver)
        mNotificationListLiveData = mNotificationViewModel.fetchNotificationList(offset, query)
        mNotificationListLiveData?.observe(viewLifecycleOwner, mNotificationListLiveDataObserver)
    }

    private fun setUpNotificationListRecyclerView() {

        val linearLayoutManager = LinearLayoutManager(requireContext())
        rvNotificationList.layoutManager = linearLayoutManager
        rvNotificationList.adapter = mAdapter

        rvNotificationList.addOnScrollListener(object :
                EndlessRecyclerOnScrollListener(linearLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                val offset = (current_page + 1) * NotificationListFragment.PER_PAGE_MESSAGE
                if (current_page * NotificationListFragment.PER_PAGE_MESSAGE < lastCount) {
                    fetchNotifications(offset)
                }
            }
        })

    }

    companion object {

        const val PER_PAGE_MESSAGE = 30

        fun newInstance(): NotificationListFragment {
            val fragment = NotificationListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}