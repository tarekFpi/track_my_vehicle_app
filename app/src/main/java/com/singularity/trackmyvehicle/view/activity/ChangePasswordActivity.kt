package com.singularity.trackmyvehicle.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_change_password.btnChangePassword
import kotlinx.android.synthetic.main.activity_change_password.containerUserVerification
import kotlinx.android.synthetic.main.activity_change_password.etConfirmPassword
import kotlinx.android.synthetic.main.activity_change_password.etCurrentPassword
import kotlinx.android.synthetic.main.activity_change_password.etNewPassword
import kotlinx.android.synthetic.main.activity_change_password.etUsername
import kotlinx.android.synthetic.main.change_password_activity.*
import javax.inject.Inject

class ChangePasswordActivity : BaseActivity() {


    @Inject
    lateinit var mViewModel: ProfileViewModel
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var userSource: UserSource

    private var mDialog: MaterialDialog? = null


    private var mChangePasswordLiveData: LiveData<Resource<GenericApiResponse<String>>>? = null


    private val mChangePasswordObserver = Observer<Resource<GenericApiResponse<String>>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Changing Password", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {
                mAnalytics.passwordChanged()
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Success",
                        data.data?.userMessage ?: "Password successfully changed")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            this.finish()
                        }
                        ?.show()
            }
            Status.ERROR -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Error",
                        data.message ?: "Could not change password.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()
        //setContentView(R.layout.activity_change_password)
        setContentView(R.layout.change_password_activity)

        btnChangePassword.setOnClickListener {
            if (validateRequestPassword()) {
                mChangePasswordLiveData?.removeObserver(mChangePasswordObserver)
                mChangePasswordLiveData = mViewModel.changePassword(
                        etUsername.text.toString(),
                        etCurrentPassword.text.toString(),
                        etNewPassword.text.toString()
                )
                mChangePasswordLiveData?.observe(this, mChangePasswordObserver)
            }
        }

        when (userSource) {
            UserSource.VERSION_3 -> {
                containerUserVerification.visibility = View.GONE
            }
            else -> {
                containerUserVerification.visibility = View.VISIBLE
            }
        }

        backPressed()

        changeEditTextBottomLineBg()
    }

    private fun changeEditTextBottomLineBg() {
        editText_prevPassword.background.mutate().setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_ATOP)

    }

    private fun backPressed() {
        imageView_backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun validateRequestPassword(): Boolean {
        if (etUsername.text.toString().isEmpty() && userSource == UserSource.VERSION_2) {
            Toasty.error(this, "Please enter a username").show()
            return false
        }
        if (etCurrentPassword.text.toString().isEmpty() && userSource == UserSource.VERSION_2) {
            Toasty.error(this, "Please enter current password").show()
            return false
        }
        if (editText_prevPassword.text.toString().isEmpty()) {
            Toasty.error(this, "Please enter previous password").show()
            return false
        }

        if (etNewPassword.text.toString().isEmpty()) {
            Toasty.error(this, "Please enter new password").show()
            return false
        }


        if (etConfirmPassword.text.toString().isEmpty()) {
            Toasty.error(this, "Please retype new password").show()
            return false
        }
        if (etConfirmPassword.text.toString() != etNewPassword.text.toString()) {
            Toasty.error(this, "The new password and retyped password do not match").show()
            return false
        }

        return true
    }

    private fun setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val transition: Transition = TransitionInflater.from(this)
                    .inflateTransition(R.transition.slide_from_bottom)
            window.enterTransition = transition
        }

    }

    override fun onResume() {
        super.onResume()
        mAnalytics.passwordChangePageViewed()
    }

    companion object {
        fun intent(context: Context) {
            context.startActivity(Intent(context, ChangePasswordActivity::class.java))
        }
    }

}
