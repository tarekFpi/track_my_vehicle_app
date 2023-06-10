package com.singularity.trackmyvehicle.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.FeedbackViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_support_form.*
import javax.inject.Inject

class SupportFormFragment : Fragment() {

    @Inject
    lateinit var mFeedBackViewModel: FeedbackViewModel
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel


    private var mFeedbackHeaders: MutableList<FeedbackHeader> = ArrayList()
    private var mVehicles: List<Terminal> = ArrayList()
    private var mSelectedVehicle: Terminal? = null
    private var mSelectedHeaders: List<FeedbackHeader> = listOf()
    private var mSupportRequestId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        mFeedBackViewModel.fetchFeedbackHeader()
                .observe(this, Observer { data ->
                    if (data == null)
                        return@Observer
                    mFeedbackHeaders = data.data?.toMutableList() ?: mutableListOf()
                    updateSelectedHeaderText()
                })
        mVehicleViewModel.getVehicles().observe(this, Observer { vehicles ->
            mVehicles = vehicles ?: listOf()
        })

        mSupportRequestId = arguments?.getString(ARG_SUPPORT_REQUEST_ID)
    }

    private fun updateSelectedHeaderText() {
        val text = mSelectedHeaders?.map { it.name }?.joinToString(",")
        btnFeedback.text = if (text.isEmpty()) "None" else text
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_support_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSelectedVehicleText()
        updateSelectedHeaderText()
        btnFeedback.setOnClickListener {
            val selectedHeaderIds = mSelectedHeaders.map { it.id }
            val selectedIndices = mutableListOf<Int>()
            mFeedbackHeaders.forEachIndexed { index, feedbackHeader ->
                if (selectedHeaderIds.contains(feedbackHeader.id)) {
                    selectedIndices.add(index)
                }
            }
            mDialog = MaterialDialog.Builder(this.context!!)
                    .title("Choose Support Category")
                    .items(mFeedbackHeaders.map { it.name })
                    .itemsCallbackMultiChoice(selectedIndices.toTypedArray()
                    ) { dialog, which, text ->
                        mSelectedHeaders = mFeedbackHeaders?.filterIndexed { index, feedbackHeader ->
                            which.contains(index)
                        }
                        updateSelectedHeaderText()
                        true
                    }
                    .positiveText(R.string.action_choose)
                    .show()
        }

        btnSelectVehicle.setOnClickListener {
            mDialog = MaterialDialog.Builder(this.context!!)
                    .title("Choose Vehicle")
                    .items(mVehicles)
                    .itemsCallbackSingleChoice(mVehicles.indexOf(mSelectedVehicle),
                            object : MaterialDialog.ListCallbackSingleChoice {
                                override fun onSelection(dialog: MaterialDialog?, itemView: View?,
                                                         which: Int, text: CharSequence?): Boolean {
                                    mSelectedVehicle = mVehicles[which]
                                    updateSelectedVehicleText()
                                    return true
                                }

                            })
                    .positiveText(R.string.action_choose)
                    .show()
        }

        btnProceed.setOnClickListener {
            postFeedback()
        }

        if(mSupportRequestId != null) {
            labelVehicle.visibility = View.GONE
            btnSelectVehicle.visibility = View.GONE
            labelSubject.visibility = View.GONE
            expenseHeaderSeparator.visibility = View.GONE
            btnFeedback.visibility = View.GONE
            labelFeedback.visibility = View.GONE
            edtSubject.visibility = View.GONE
        }

    }

    private fun updateSelectedVehicleText() {
        btnSelectVehicle.text = mSelectedVehicle?.toString() ?: "No vehicle selected"
    }

    private var mCreateFeedbackLiveData: LiveData<Resource<GenericApiResponse<String>>>? = null

    private var mDialog: MaterialDialog? = null


    private val mFeedbackAddObserver = Observer<Resource<GenericApiResponse<String>>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(activity!!, "Feedback Posting",
                        "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(activity!!, "Success",
                        data.data?.userMessage ?: "Feedback added successfully")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            mCallback.feedbackAdded()
                        }
                        ?.show()
                mAnalytics.myFeedbackAdded()
            }
            Status.ERROR -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(activity!!, "Error",
                        data.message ?: "Could not add feedback.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
                mAnalytics.myFeedbackDidNotAdd()
            }
        }
    }

    private fun postFeedback() {

        mCreateFeedbackLiveData?.removeObserver(mFeedbackAddObserver)
        mCreateFeedbackLiveData = mFeedBackViewModel.createSupport(
                mSelectedVehicle?.terminalID?.toString() ?: "",
                mSelectedHeaders.joinToString { it.id },
                edtSubject.text.toString(),
                edtMessage.text.toString(),
                mSupportRequestId)
        mCreateFeedbackLiveData
                ?.observe(this, mFeedbackAddObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    companion object {
        private const val ARG_SUPPORT_REQUEST_ID = "ARG_SUPPORT_REQUEST_ID"
        fun newInstance(supportRequestId: String? = null): SupportFormFragment {
            val fragment = SupportFormFragment()
            val args = Bundle()
            supportRequestId?.let {
                args.putString(ARG_SUPPORT_REQUEST_ID,it)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.myFeedbackAddScreenViewed()

    }

    private lateinit var mCallback: FeedbackFormFragment.Callback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FeedbackFormFragment.Callback) {
            mCallback = context
            return
        }
        Log.w(this.javaClass.name, "Callback interface is not implemented")
    }

}
