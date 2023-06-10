package com.singularity.trackmyvehicle.view.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_forget_password.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ForgetPasswordActivity : BaseActivity() {

    var STATUS: ForgetActivityState = ForgetActivityState.REQUESTING_OTP

    @Inject lateinit var mViewModel: ProfileViewModel
    @Inject lateinit var mAnalytics: AnalyticsViewModel
    private var mDialog: MaterialDialog? = null

    private var mPasswordToken: String = ""
    private var mUserNameOrEmail: String = ""

    private var count_time = 0

    private lateinit var appPreference : AppPreference

    private lateinit var TextviewTimer_score:TextView

    private lateinit var text_otpResend:TextView


    private val mOtpRequestObserver = Observer<Resource<OtpResponse>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Requesting Password Reset", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {

                mAnalytics.otpRequested()

                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Success", data.data?.userMessage ?: "Go ahead and enter opt")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            if(data.data?.context == "otp-v3") {
                                // V3 Response
                                mPasswordToken = data.data.otpToken
                                STATUS = ForgetActivityState.VALIDATING_OTP_V3
                                showOtpValidatorV3()

                                //Otp 10 Minutes function count call
                                ResendCount_Timer()

                                appPreference.SetuserName(AppPreference.UserName,etUsername.text.toString())
                                appPreference.Setphone(AppPreference.phone,etMobileNumber.text.toString())


                                return@onPositive
                            }


                            showOtpValidator()
                            STATUS = ForgetActivityState.VALIDATING_OTP
                        }
                        ?.show()
            }
            Status.ERROR   -> {

                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Error", data.message ?: "Could not request password reset.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
            }
        }
    }


    private var mRequestOtpLiveData: MutableLiveData<Resource<OtpResponse>>? = null

    private val mOtpValidateObserver = Observer<Resource<OtpValidationResponse>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {

                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Validating OTP", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {

                mAnalytics.otpValidated()
                mPasswordToken = data?.data?.passwordToken ?: ""

                mDialog?.dismiss()
                showNewPasswordContainer()
                STATUS = ForgetActivityState.RESETTING_PASSWORD
            }
            Status.ERROR   -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Error", data.message ?: "Could not validate OTP.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
            }
        }
    }


    private var mOtpValidateLiveData: MutableLiveData<Resource<OtpValidationResponse>>? = null

    private val mPasswordResetObserver = Observer<Resource<GenericApiResponse<String>>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Resetting Password", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {
                mAnalytics.passwordReset()
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Success", data.data?.userMessage ?: "Password Reset Successfully")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            this.finish()
                        }
                        ?.show()
            }
            Status.ERROR   -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Error", data.message ?: "Could not validate OTP.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
            }
        }
    }
    private var mPasswordResetLiveData: MutableLiveData<Resource<GenericApiResponse<String>>>? = null

    private val mPasswordResetObserverV3 = Observer<Resource<GenericApiResponse<String>>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this, "Resetting Password", "Hold On...")
                        ?.show()
            }
            Status.SUCCESS -> {
                mAnalytics.passwordReset()
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Success", data.data?.userMessage ?: "Password Reset Successfully")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which ->
                            dialog.dismiss()
                            this.finish()
                        }
                        ?.show()
            }
            Status.ERROR   -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this, "Error", data.message ?: "Could not validate OTP.")
                        ?.positiveText("OK")
                        ?.onPositive { dialog, which -> dialog.dismiss() }
                        ?.show()
            }
        }
    }
    private var mPasswordResetLiveDataV3: MutableLiveData<Resource<GenericApiResponse<String>>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()
        setContentView(R.layout.activity_forget_password)

        text_otpResend =findViewById(R.id.text_otpResend)

        TextviewTimer_score =findViewById(R.id.text_otpResendTime)

        appPreference = AppPreferenceImpl(applicationContext)

        text_otpResend.setOnClickListener {


            text_otpResend.visibility=View.GONE

            TextviewTimer_score.visibility = View.VISIBLE

           // if (count_time != 3) {

                //Otp 10 Minutes function count call
                ResendCount_Timer()

                //Otp Resend function call
                ResedOTp_Recalled()


        }



        ExpectAnim()
                .expect(containerOtpValidate)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .expect(containerOtpValidateV3)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .expect(containerNewPassword)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .toAnimation()
                .setNow()


        btnResetPassword.setOnClickListener {
            if (validateRequestPassword()) {

                mRequestOtpLiveData?.removeObserver(mOtpRequestObserver)
                mUserNameOrEmail = etUsername.text.toString()
                mRequestOtpLiveData = mViewModel.requestForgetPasswordOtp(
                        etUsername.text.toString(),
                        etMobileNumber.text.toString()
                )
                mRequestOtpLiveData?.observe(this, mOtpRequestObserver)
            }
        }

        btnValidateOtp.setOnClickListener {
            if (etOtp.text.toString().isEmpty()) {
                Toasty.error(this, "Enter valid OTP").show()
                return@setOnClickListener
            }
            mOtpValidateLiveData?.removeObserver(mOtpValidateObserver)
            mOtpValidateLiveData = mViewModel.validateOtp(etOtp.text.toString())
            mOtpValidateLiveData?.observe(this, mOtpValidateObserver)

        }

        btnUpdatePassword.setOnClickListener {
            if (etNewPassword.text.toString() != etConfirmPassword.text.toString()) {
                Toasty.error(this, "Passwords doesn't match. Please try again.").show()
                return@setOnClickListener
            }

            mPasswordResetLiveData?.removeObserver(mPasswordResetObserver)
            mPasswordResetLiveData = mViewModel.resetPassword(
                    etUsername.text.toString(),
                    mPasswordToken,
                    etNewPassword.text.toString()
            )
            mPasswordResetLiveData?.observe(this, mPasswordResetObserver)
        }

        btnUpdatePasswordV3.setOnClickListener {
            if (etOtpV3.text.toString().isEmpty()) {
                Toasty.error(this, "Enter valid OTP").show()
                return@setOnClickListener
            }

            if (etNewPasswordV3.text.toString().isEmpty()) {
                Toasty.error(this, "Enter a Passwords.").show()
                return@setOnClickListener
            }

            if (etNewPasswordV3.text.toString() != etConfirmPasswordV3.text.toString()) {
                Toasty.error(this, "Passwords doesn't match. Please try again.").show()
                return@setOnClickListener
            }

            mPasswordResetLiveDataV3?.removeObserver(mPasswordResetObserverV3)
            mPasswordResetLiveDataV3 = mViewModel.resetPasswordV3(
                    mUserNameOrEmail,
                    etOtpV3.text.toString(),
                    mPasswordToken,
                    etNewPasswordV3.text.toString()
            )
            mPasswordResetLiveDataV3?.observe(this, mPasswordResetObserverV3)
        }


    }


  private  fun ResedOTp_Recalled(){

        var UserName:String= appPreference.getUserName(AppPreference.UserName).toString()

        var phone:String = appPreference.getphone(AppPreference.phone).toString()


   mRequestOtpLiveData?.removeObserver(mOtpRequestObserver)
        mUserNameOrEmail = etUsername.text.toString()
        mRequestOtpLiveData = mViewModel.requestForgetPasswordOtp(
            UserName,
            phone
        )
        mRequestOtpLiveData?.observe(this, mOtpRequestObserver)
    }


    private fun ResendCount_Timer() {

        //val timer = object: CountDownTimer(600000, 1000) {

        val timer = object: CountDownTimer(600000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                TextviewTimer_score.text = "" + String.format(
                    "0%d : %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
            }

            override fun onFinish() {

            /*    count_time = count_time + 1
                if (count_time ==3) {
                    TextviewTimer_score.text = "Sorrey Resend Time Over!!"
                }*/

                TextviewTimer_score.text = "0.0 min"
                TextviewTimer_score.visibility=View.GONE

                text_otpResend.visibility=View.VISIBLE

            }
        }
        timer.start()

    }

    private fun validateRequestPassword(): Boolean {
        if (etUsername.text.toString().isEmpty()) {
            Toasty.error(this, "Please enter a username").show()
            return false
        }
        /*if (etMobileNumber.text.toString().isEmpty()) {
            Toasty.error(this, "Please enter a mobile number").show()
            return false
        }

        if ((! etMobileNumber.text.toString().startsWith("+880") &&
                ! etMobileNumber.text.toString().startsWith("880") &&
                ! etMobileNumber.text.toString().startsWith("01"))

                ) {
            Toasty.error(this, "Please enter a valid mobile number").show()
            return false
        }*/
        return true
    }

    private fun setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val transition: Transition
            transition = TransitionInflater.from(this).inflateTransition(R.transition.slide_from_bottom)
            window.enterTransition = transition
        }
    }

    companion object {
        fun intent(context: Context) {
            context.startActivity(Intent(context, ForgetPasswordActivity::class.java))
        }
    }


    /**
     * step 2 to 1
     */
    private fun hideOTPValidator() {
        ExpectAnim()
                .expect(containerOtpValidate)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .expect(containerOtpValidateV3)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .expect(containerResetPassword)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }


    /**
     * step 3 to 2
     */
    private fun hideNewPassword() {
        ExpectAnim()
                .expect(containerNewPassword)
                .toBe(Expectations.outOfScreen(Gravity.END))
                .expect(containerOtpValidate)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }

    /**
     * step  1 to 2
     */
    private fun showOtpValidator() {
        ExpectAnim()
                .expect(containerResetPassword)
                .toBe(Expectations.outOfScreen(Gravity.START))
                .expect(containerOtpValidate)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }

    /**
     * step  1 to 2
     */
    private fun showOtpValidatorV3() {
        ExpectAnim()
                .expect(containerResetPassword)
                .toBe(Expectations.outOfScreen(Gravity.START))
                .expect(containerOtpValidateV3)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }

    /**
     * step 2 to 3
     */
    private fun showNewPasswordContainer() {
        ExpectAnim()
                .expect(containerOtpValidate)
                .toBe(Expectations.outOfScreen(Gravity.START))
                .expect(containerNewPassword)
                .toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }


    override fun onBackPressed() {
        when (STATUS) {
            ForgetActivityState.REQUESTING_OTP     -> {
                super.onBackPressed()
                return
            }
            ForgetActivityState.VALIDATING_OTP     -> {
                STATUS = ForgetActivityState.REQUESTING_OTP

                hideOTPValidator()
                return
            }
            ForgetActivityState.RESETTING_PASSWORD -> {
                STATUS = ForgetActivityState.VALIDATING_OTP

                hideNewPassword()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.forgetPasswordScreenViewed()
    }

    enum class ForgetActivityState {
        REQUESTING_OTP,
        VALIDATING_OTP,
        RESETTING_PASSWORD,
        VALIDATING_OTP_V3,
    }
}
