package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import com.singularity.trackmyvehicle.view.activity.BillingActivity
import com.singularity.trackmyvehicle.view.activity.ChangePasswordActivity
import com.singularity.trackmyvehicle.view.activity.FeedbackActivity
import com.singularity.trackmyvehicle.view.activity.SecureModeActivity
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_account.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class AccountFragment : Fragment() {

    @Inject lateinit var mProfileViewModel: ProfileViewModel
    @Inject lateinit var mAnalytics: AnalyticsViewModel
    @Inject lateinit var userSource: UserSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUi(mProfileViewModel.getProfile())

        mProfileViewModel.fetchProfileInformation().observe(this,
                Observer { data ->
                    if (data == null)
                        return@Observer
                    if (data.data != null) {
                        updateUi(data.data)
                    }
                })

        when(userSource){
            UserSource.VERSION_3 -> {
                txtLabelAddress.visibility =View.GONE
                txtAddress.visibility =View.GONE
            }
            else -> {
                txtLabelAddress.visibility =View.VISIBLE
                txtAddress.visibility =View.VISIBLE
            }
        }

        btnChangePassword.setOnClickListener {
            ChangePasswordActivity.intent(this.context !!)

        }
        btnBilling.setOnClickListener {
            BillingActivity.intent(this.context !!)
        }

        btnAddFeedback.setOnClickListener {
            FeedbackActivity.intent(this.context !!, FeedbackActivity.TAG_FEEDBACK_ADD)
        }

        btnFeedbackList.setOnClickListener {
            FeedbackActivity.intent(this.context !!, FeedbackActivity.TAG_FEEDBACK_LIST)
        }

        btnLogout.setOnClickListener {
            mAnalytics.logOut()
            mProfileViewModel.logout {}
            EventBus.getDefault().post(LogoutEvent())
        }

        btnRemoteEngineDisarm.setOnClickListener {
            this.context?.let { it1 -> SecureModeActivity.intent(it1) }
        }
    }

    private fun updateUi(profile: Profile?) {
        txtName.text = if (profile?.name?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.name
        }
        txtEmail.text = if (profile?.email?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.email
        }
        txtMobile.text = if (profile?.mobile?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.mobile
        }
        txtAddress.text = if (profile?.address?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.address
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    companion object {
        fun newInstance(): AccountFragment {
            val fragment = AccountFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.accountFragmentViewed()
    }
}