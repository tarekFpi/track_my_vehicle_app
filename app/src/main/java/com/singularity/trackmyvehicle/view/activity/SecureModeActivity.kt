package com.singularity.trackmyvehicle.view.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.SecureModeReponse
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.SecureModeViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import kotlinx.android.synthetic.main.activity_secure_mode.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject


class SecureModeActivity : BaseActivity() {


    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var mViewModel: SecureModeViewModel

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_mode)

        updateHeader()

        updateUI(null)

        btnRefresh.setOnClickListener {
            fetchStatus()
        }
        vehicleSelectionToolbarView.setChangeVehicleClickListener(View.OnClickListener {
            showVehicleSelectFragment()
        })
        vehicleSelectionToolbarView.setImgBackClickListener(View.OnClickListener {
            this.finish()
        })

        switchRemoteEngineDisarm.setOnCheckedChangeListener { compoundButton, bool ->
            showPasswordChangeDialog(bool)
        }
    }

    private fun showPasswordChangeDialog(bool: Boolean) {
        if ((mLastData?.data?.secure == 1) == bool)
            return
        mDialog = DialogHelper.showInputPasswordDialog(this, { password ->
            updateStatus(password, bool)
        }, {
            updateUI(mLastData)
        })?.show()
    }

    private fun updateStatus(password: String, bool: Boolean) {
        mLiveData?.removeObserver(mSecureModeObserver)
        mLiveData = mViewModel.secureMode(if (bool) "1" else "0", password)
        mLiveData?.observe(this, mSecureModeObserver)
    }

    private var mLastData: GenericApiResponse<SecureModeReponse>? = null
    private var mLiveData: LiveData<Resource<GenericApiResponse<SecureModeReponse>>>? = null

    private val mSecureModeObserver = Observer<Resource<GenericApiResponse<SecureModeReponse>>> { data ->
        if (data == null)
            return@Observer

        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Please Wait", "Fetching Remote Engine disarm status")
                        ?.show()
            }
            Status.ERROR   -> {
                updateUI(null)
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this@SecureModeActivity,
                        "Error",
                        data?.message
                                ?: "Could not fetch remote engine disarm status. Hit refresh to try again."
                )?.negativeText("OK")
                        ?.show()
            }
            Status.SUCCESS -> {
                updateUI(data.data)
                mDialog?.dismiss()
            }
        }
    }

    private fun updateUI(data: GenericApiResponse<SecureModeReponse>?) {
        if (data == null) {
            // Error/ Empty
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            imgStatus.colorFilter = filter
            switchRemoteEngineDisarm.text = "Status could not be fetched"
            switchRemoteEngineDisarm.isEnabled = false
            return
        }
        mLastData = data
        val matrix = ColorMatrix()
        matrix.setSaturation(1f)
        val filter = ColorMatrixColorFilter(matrix)
        imgStatus.colorFilter = filter
        switchRemoteEngineDisarm.isEnabled = true
        switchRemoteEngineDisarm.isChecked = data.data?.secure == 1
        switchRemoteEngineDisarm.text = if (data.data?.secure == 1)
            getString(R.string.msg_remote_engine_disarm_is_on)
        else
            getString(R.string.msg_remote_engine_disarm_is_off)

        imgStatus.setImageDrawable(if (data.data?.secure == 1)
            ContextCompat.getDrawable(this@SecureModeActivity, R.drawable.img_locked)
        else
            ContextCompat.getDrawable(this@SecureModeActivity, R.drawable.img_unlocked)
        )


    }

    private fun fetchStatus() {
        mLiveData?.removeObserver(mSecureModeObserver)
        mLiveData = mViewModel.secureMode()
        mLiveData?.observe(this, mSecureModeObserver)
    }

    private fun showVehicleSelectFragment() {
        val dialogFrag = BottomNavFragment.newInstance();
        dialogFrag.show(supportFragmentManager, dialogFrag.getTag())
    }

    companion object {
        fun intent(context: Context) {
            context.startActivity(Intent(context, SecureModeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (! EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mAnalytics.secureModeViewed()
        fetchStatus()
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

    }

    private fun updateHeader() {
        vehicleSelectionToolbarView.setCurrentVehicleId(mPrefRepository.currentVehicle())
        fetchStatus()
    }

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        mLastData = null
        updateUI(mLastData)
        updateHeader()
    }
}
