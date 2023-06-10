package com.singularity.trackmyvehicle.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.Notification
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.implementation.v3.NotificationResponseWrapper
import com.singularity.trackmyvehicle.view.adapter.NotificationListAdapter
import com.singularity.trackmyvehicle.view.customview.EndlessRecyclerOnScrollListener
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.NotificationViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_notification_list.*
import javax.inject.Inject


class NotificationListFragment : Fragment(), OnItemClickCallback<Notification> {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var mNotificationViewModel: NotificationViewModel

    private var mAdapter: NotificationListAdapter = NotificationListAdapter(this)

    private var mData: MutableList<Notification> = mutableListOf()

    private var lastCount = 0

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
        return inflater.inflate(R.layout.fragment_notification_list, container, false)
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
                val offset = (current_page + 1) * PER_PAGE_MESSAGE
                if (current_page * PER_PAGE_MESSAGE < lastCount) {
                    fetchNotifications(offset)
                }
            }
        })

    }

    override fun onClick(model: Notification) {
        //TODO: Implement notification background change after it has been read
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