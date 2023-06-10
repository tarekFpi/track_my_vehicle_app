package com.singularity.trackmyvehicle.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.FeedbackRemarkListAdapter
import com.singularity.trackmyvehicle.viewmodel.FeedbackViewModel
import kotlinx.android.synthetic.main.fragment_feedback_view.*
import org.joda.time.IllegalInstantException
import javax.inject.Inject

class FeedbackViewFragment : Fragment() {

    private var mFeedbackRemarkLiveData: LiveData<Resource<List<FeedbackRemark>>>? = null

    private var mFeedbackId: String = ""
    private var mData: MutableList<FeedbackRemark>? = null
    @Inject
    lateinit var mFeedbackViewModel: FeedbackViewModel

    @Inject
    lateinit var mAdapter: FeedbackRemarkListAdapter
    @Inject
    lateinit var userSource: UserSource

    var callback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VehicleTrackApplication.appComponent?.inject(this)

        if (arguments == null) {
            throw IllegalInstantException("Argument cannot be null")
        }

        mFeedbackId = arguments?.getString(ARG_FEEDBACK_ID, "") ?: ""

        if (mFeedbackId.isEmpty()) {
            throw IllegalInstantException("Feedback Id cannot be empty")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_feedback_view, container, false)
    }

    private var mFeedbackRemarkObserver: Observer<Resource<List<FeedbackRemark>>> = Observer<Resource<List<FeedbackRemark>>> { data ->
        if (data == null)
            return@Observer

        when (data.status) {
            Status.LOADING -> {
                txtEmpty.text = "Remarks are loading"
                swipeRefreshLayout.isRefreshing = true
            }
            Status.SUCCESS -> {
                txtEmpty.text = "Remarks list seems empty right now"
                swipeRefreshLayout.isRefreshing = false
            }
            Status.ERROR   -> {
                txtEmpty.text = "Remarks list could not be loaded"
                swipeRefreshLayout.isRefreshing = false
            }
        }

        if (data.data?.size == 0) {
            showEmptyListView()
            return@Observer

        } else {
            hideEmptyListView()
        }
        mData = data.data?.toMutableList()
        updateList(mData)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = mAdapter
        swipeRefreshLayout.setOnRefreshListener {
            fetchFeedbackRemarks()
        }
        updateList(mData)

        if (userSource.identifier == UserSource.VERSION_2.identifier) {
            fabReply.visibility = View.GONE
        }

        fabReply.setOnClickListener {
            callback?.onReplyClicked(mFeedbackId)
        }
    }

    private fun fetchFeedbackRemarks() {
        mFeedbackRemarkLiveData?.removeObserver(mFeedbackRemarkObserver)
        mFeedbackRemarkLiveData = mFeedbackViewModel.fetchFeedbackRemarks(mFeedbackId)
        mFeedbackRemarkLiveData?.observe(this, mFeedbackRemarkObserver)
    }

    private fun showEmptyListView() {
        txtEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyListView() {
        txtEmpty.visibility = View.GONE
    }


    private fun updateList(data: MutableList<FeedbackRemark>?) {
        mAdapter.setData(data)
    }

    override fun onPause() {
        super.onPause()
        mAdapter.setData(ArrayList())
    }

    override fun onResume() {
        super.onResume()
        fetchFeedbackRemarks()
    }

    interface Callback {
        fun onReplyClicked(requestId: String)
    }

    companion object {

        val ARG_FEEDBACK_ID = "ARG_FEEDBACK_ID"

        fun newInstance(feedbackId: String): FeedbackViewFragment {
            val fragment = FeedbackViewFragment()
            val args = Bundle()
            args.putString(ARG_FEEDBACK_ID, feedbackId)
            fragment.arguments = args
            return fragment
        }
    }

}
