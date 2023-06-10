package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.FeedbackListAdapter
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.FeedbackViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_feedback_list.*
import javax.inject.Inject

class FeedbackListFragment : Fragment(), OnItemClickCallback<Feedback> {
    @Inject lateinit var mFeedbackViewModel: FeedbackViewModel

    @Inject lateinit var mAdapter: FeedbackListAdapter

    private var mCallback: FeedbackListFragment.FeedbackListCallback? = null
    @Inject lateinit var mAnalytics: AnalyticsViewModel


    private val mFeedbackObserver = Observer<MutableList<Feedback>> { data ->
        if (data == null)
            return@Observer

        if (data.size == 0) {
            showEmptyListView()
        } else {
            hideEmptyListView()
        }
        updateList(data)
    }

    private var mFeedbackLiveData: LiveData<MutableList<Feedback>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        fetchFeedBacks()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.adapter = mAdapter
        mAdapter.callback = this
        list.layoutManager = LinearLayoutManager(this.context)
        list.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        mFeedbackLiveData?.removeObserver(mFeedbackObserver)
        mFeedbackLiveData = mFeedbackViewModel.getFeedback()
        mFeedbackLiveData?.observe(this, mFeedbackObserver)

        fabAdd.setOnClickListener {
            mCallback?.feedbackAddClicked()
        }

        swipeRefreshLayout.setOnRefreshListener { refresh() }

    }

    private var mFeedbackStatusLiveData: LiveData<Resource<List<Feedback>>>? = null
    private val mFetchFeedbackStatusObserver = Observer<Resource<List<Feedback>>> { data ->
        if (data == null)
            return@Observer

        when (data.status) {
            Status.SUCCESS, Status.ERROR -> swipeRefreshLayout.isRefreshing = false
            Status.LOADING               -> swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun refresh() {
        mFeedbackStatusLiveData?.removeObserver(mFetchFeedbackStatusObserver)
        mFeedbackStatusLiveData = mFeedbackViewModel.fetchSinglePageFeedbackWithStatus()
        mFeedbackStatusLiveData?.observe(this, mFetchFeedbackStatusObserver)
    }


    private fun fetchFeedBacks() {
        mFeedbackViewModel.fetchFeedback()
    }

    private fun showEmptyListView() {
        txtEmpty.visibility = View.VISIBLE
        list.visibility = View.GONE
    }

    private fun hideEmptyListView() {
        txtEmpty.visibility = View.INVISIBLE
        list.visibility = View.VISIBLE
    }

    private fun updateList(data: MutableList<Feedback>) {
        mAdapter.setData(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    override fun onClick(model: Feedback) {
        mCallback?.feedbackItemClicked(model)
    }

    companion object {
        fun newInstance(): FeedbackListFragment {
            val fragment = FeedbackListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FeedbackListCallback) {
            mCallback = context
            return
        }

        Log.w(this.javaClass.name, "FeedbackListCallback interface is not implemented")
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.myFeedbackScreenViewed()
    }

    interface FeedbackListCallback {
        fun feedbackItemClicked(feedback: Feedback)
        fun feedbackAddClicked()
    }
}
