package com.singularity.trackmyvehicle.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.gson.GsonBuilder
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v3.EventsVehicleRouteAnalyticsItem
import com.singularity.trackmyvehicle.view.adapter.EventsVehicleRouteAnalyticsAdapter
import com.singularity.trackmyvehicle.view.viewCallback.EventListCallback
import kotlinx.android.synthetic.main.fragment_event_list.*

/**
 * Created by Kariba Yasmin on 9/2/21.
 */
class EventListFragment : DialogFragment() {

    private var eventList: ArrayList<EventsVehicleRouteAnalyticsItem> = ArrayList()

    private lateinit var eventAdapter : EventsVehicleRouteAnalyticsAdapter

    companion object {
        const val TAG = "EventListFragment"
        lateinit var listener: EventListCallback

        fun newInstance(eventList: String, listener: EventListCallback): EventListFragment {
            val args = Bundle()
            args.putString("data", eventList)

            val fragment = EventListFragment()
            fragment.arguments = args

            this.listener = listener

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.getWindow()?.setBackgroundDrawableResource(R.color.analyticsVehicleRouteBgColor);
        dialog?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setupView(view)
    }

    private fun setupView(view: View) {
        val data = arguments?.getString("data")
        val list = GsonBuilder().create().fromJson(data, Array<EventsVehicleRouteAnalyticsItem>::class.java)
        eventList = list.toCollection(ArrayList())

        eventAdapter = EventsVehicleRouteAnalyticsAdapter(view.context, eventList)
        recyclerView_events.setHasFixedSize(true)
        recyclerView_events.adapter = eventAdapter

        layout_add_events.setOnClickListener {
            listener.onEventListFound(eventList)
            dismiss()
        }

        imageView_cross.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()?.setLayout(width, height)
        }
    }

}