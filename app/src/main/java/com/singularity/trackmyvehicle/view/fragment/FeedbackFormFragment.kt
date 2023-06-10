package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.FeedbackViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_feedback_form.*
import javax.inject.Inject

class FeedbackFormFragment : Fragment() {

    @Inject lateinit var mFeedBackViewModel: FeedbackViewModel
    @Inject lateinit var mPrefRepository: PrefRepository
    @Inject lateinit var mAnalytics: AnalyticsViewModel
    @Inject lateinit var mVehicleViewModel: VehiclesViewModel
    private var mVehicles: List<Terminal> = ArrayList()
    private var mSelectedVehicle: Terminal? = null

    private var mFeedbackHeaders: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        mFeedBackViewModel.fetchFeedbackHeader()
                .observe(this, Observer { data ->
                    if (data == null)
                        return@Observer
                    data?.data?.forEach { header ->
                        mFeedbackHeaders?.add(header.name)
                        mSelectedHeader = header?.name
                    }
                    btnFeedback.text = mSelectedHeader
                })
        mVehicleViewModel.getVehicles().observe(this, Observer { vehicles ->
            mVehicles = vehicles ?: listOf()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_feedback_form, container, false)
    }


    private var mSelectedHeader: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFeedback.setOnClickListener {
            mDialog = MaterialDialog.Builder(this.context !!)
                    .title("Choose Feedback Header")
                    .items(mFeedbackHeaders)
                    .itemsCallbackSingleChoice(- 1, MaterialDialog.ListCallbackSingleChoice { dialog, view, which, text : CharSequence? ->
                        mSelectedHeader = text?.toString() ?: ""
                        btnFeedback.text = mSelectedHeader
                        true
                    })
                    .positiveText(R.string.action_choose)
                    .show()
        }

        btnProceed.setOnClickListener {
            postFeedback()
        }

        btnSelectVehicle.setOnClickListener {
            mDialog = MaterialDialog.Builder(this.context!!)
                    .title("Choose Vehicle")
                    .items(mVehicles)
                    .itemsCallbackSingleChoice(mVehicles.indexOf(mSelectedVehicle)
                    ) { dialog, itemView, which, text ->
                        if(which < mVehicles.size ) {
                            mSelectedVehicle = mVehicles[which]
                            updateSelectedVehicleText()
                        }
                        true
                    }
                    .positiveText(R.string.action_choose)
                    .show()
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
                mDialog = DialogHelper.getLoadingDailog(activity !!, "Feedback Posting", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(activity !!, "Success", data.data?.userMessage ?: "Feedback added successfully")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            mCallback.feedbackAdded()
                        }
                        ?.show()
                mAnalytics.myFeedbackAdded()
            }
            Status.ERROR   -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(activity !!, "Error", data.message ?: "Could not add feedback.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
                mAnalytics.myFeedbackDidNotAdd()
            }
        }
    }

    private fun postFeedback() {

        if(mSelectedVehicle == null) {
            Toasty.error(requireContext(), "Select Vehicle First", Toast.LENGTH_SHORT).show()
            return
        }

        mCreateFeedbackLiveData?.removeObserver(mFeedbackAddObserver)
        mCreateFeedbackLiveData = mFeedBackViewModel.createFeedback(
                mSelectedVehicle?.bstid ?: "",
                mSelectedHeader,
                edtDetails.text.toString())
        mCreateFeedbackLiveData
                ?.observe(this, mFeedbackAddObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    companion object {
        fun newInstance(): FeedbackFormFragment {
            val fragment = FeedbackFormFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.myFeedbackAddScreenViewed()

    }

    private lateinit var mCallback: Callback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
            mCallback = context
            return
        }
        Log.w(this.javaClass.name, "Callback interface is not implemented")
    }

    interface Callback {
        fun feedbackAdded()
    }

}
